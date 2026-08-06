package match.service.service.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import match.service.client.UserServiceClient;
import match.service.dto.MatchScoreRequest;
import match.service.dto.MatchScoreResponse;
import match.service.dto.ResumeGenerationRequest;
import match.service.dto.ResumeGenerationResponse;
import match.service.exceptions.ResumeContentNotFoundException;
import match.service.service.MatchService;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.List;


@Service
@AllArgsConstructor
@Slf4j
public class MatchServiceImpl implements MatchService {

    private final OllamaChatModel chatModel;
    private final OllamaEmbeddingModel embeddingModel;
    private final UserServiceClient userServiceClient;
    private final ObjectMapper objectMapper;

    @Value("${app.generation.temperature}")
    private double temp;

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

    @Override
    public ResumeGenerationResponse generateTailoredResume(ResumeGenerationRequest request, String authToken) {
        Long resumeId = request.getResumeId();
        String resumeParsedText = userServiceClient.getResumeText(resumeId, authToken);
        if( resumeParsedText == null || resumeParsedText.isBlank()){
            throw new ResumeContentNotFoundException("Resume content could not be found or is empty for resume ID: " + request.getResumeId());
        }
        String promptMessage = promptBuilder(request, resumeParsedText);

        Prompt prompt = new Prompt(
            promptMessage,
            OllamaOptions.builder()
            .temperature(temp)
            .build()
        );
        try{

        String ollamaResponseContent = chatModel.call(prompt).getResult().getOutput().getText();
       log.info("LLM raw tailored resume response: {}", ollamaResponseContent);
       String cleanedResponse = ollamaResponseContent.replace("```jaon","")
       .replace("```", "")
       .trim();

        ResumeGenerationResponse response = objectMapper.readValue(cleanedResponse, ResumeGenerationResponse.class);
        response.setGeneratedAt(LocalDateTime.now());
        return response; 
    } catch( Exception e){
       log.error("Failed to generate or parse tailored resume from LLM", e);
            throw new RuntimeException("Failed to generate tailored resume. Please try again later.", e); 
    }
}

      public String promptBuilder(ResumeGenerationRequest request, String resumeParsedText){
           return String.format(
            """
            You are an expert career consultant and professional resume writer. 
            Your task is to tailor the candidate's base resume to align closely with a specific job description.
            
            Target Job Details:
            - Company Name: %s
            - Job Title: %s
            - Job Description: 
            %s
            
            Candidate's Original Resume Text:
            %s
            
            Instructions:
            1. Rewrite and optimize the resume text to highlight relevant skills, achievements, and keywords matching the target job description. Maintain absolute truthfulness to the candidate's original background.
            2. Write a professional matching cover letter.
            3. List the core key changes made to the existing resume text.
            4.Reorder experience bullet points to highlight most relevant ones first, incorporate keywords from the JD naturally (not keyword stuffing), keep all factual information accurate (never invent experience), adjust summary section to match the role, identify 3-5 key changes made.
            
            Respond ONLY in raw JSON matching this exact structure (no markdown formatting, no code block backticks):
            {
                "tailoredResume": "Full text of the tailored resume here...",
                "coverLetter": "Full text of the cover letter here...",
                "keyChanges": ["Change 1", "Change 2", "Change 3"]
            }
            """,
            request.getCompanyName(),
            request.getJobTitle(),
            request.getJobDescription(),
            resumeParsedText
        );

      }

}
