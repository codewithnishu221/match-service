package match.service.service.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import match.service.client.UserServiceClient;
import match.service.dto.MatchScoreRequest;
import match.service.dto.MatchScoreResponse;
import match.service.service.MatchService;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@AllArgsConstructor
@Slf4j
public class MatchServiceImpl implements MatchService {

    private final OllamaChatModel chatModel;
    private final OllamaEmbeddingModel embeddingModel;
    private final UserServiceClient userServiceClient;
    private final ObjectMapper objectMapper;

    @Override
    public MatchScoreResponse calculateMatchScore(MatchScoreRequest request, String authToken) {
        String resumeText = userServiceClient.getResumeText(request.getResumeId(), authToken);
        if (resumeText == null) {
            log.error("Could not fetch resume text for resumeId: {}", request.getResumeId());
            return new MatchScoreResponse(0.0, "Could not retrieve resume for analysis", List.of());
        }
        String jobDescriptionText = request.getJobDescription();
        float[] resumeEmbedding = embeddingModel.embed(resumeText);
        float[] jdEmbedding = embeddingModel.embed(jobDescriptionText);

        double similarityScore = cosineSimilarity(resumeEmbedding, jdEmbedding) * 100;
        log.info("Cosine similarity score: {}", similarityScore);

        String prompt = """
        You are an expert technical recruiter and resume screening specialist.
        
        RESUME TEXT:
        %s
        
        JOB DESCRIPTION:
        %s
        
        Analyze the candidate's resume against the job description.
        1. Determine missing skills: identify key technical skills, tools, or requirements in the Job Description that are NOT mentioned or implied in the Resume.
        2. Provide a 2-3 sentence explanation of the match.
        
        Respond ONLY in raw JSON matching this structure (no markdown formatting, no code block backticks):
        {
            "score": 0.0,
            "explanation": "your explanation here",
            "missingSkills": ["Skill1", "Skill2", "Skill3"]
        }
        """.formatted(resumeText, jobDescriptionText);
        try {
            String llmResponse = chatModel.call(prompt);
            log.info("LLM raw response: {}", llmResponse);
            String cleanedResponse = llmResponse
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            MatchScoreResponse llmResult = objectMapper.readValue(cleanedResponse, MatchScoreResponse.class);

            llmResult.setScore(Math.round(similarityScore * 10.0) / 10.0);
            return llmResult;
        } catch (Exception e) {
            log.error("Failed to parse LLM response, falling back to similarity score", e);
            return new MatchScoreResponse(
                    Math.round(similarityScore * 10.0) / 10.0,
                    "Match score calculated based on skill similarity analysis.", List.of()
            );
        }
    }

    private double cosineSimilarity(float[] vectorA, float[] vectorB) {
        double dotProduct = 0.0;
        double magnitudeA = 0.0;
        double magnitudeB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            magnitudeA += vectorA[i] * vectorA[i];
            magnitudeB += vectorB[i] * vectorB[i];
        }
        magnitudeA = Math.sqrt(magnitudeA);
        magnitudeB = Math.sqrt(magnitudeB);

        if (magnitudeA == 0 || magnitudeB == 0) return 0.0;

        return dotProduct / (magnitudeA * magnitudeB);
    }

}
