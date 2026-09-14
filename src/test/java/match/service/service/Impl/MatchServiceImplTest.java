package match.service.service.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import match.service.client.UserServiceClient;
import match.service.dto.*;
import match.service.exceptions.ResumeContentNotFoundException;
import match.service.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchServiceImplTest {

    @Mock
    private OllamaChatModel chatModel;

    @Mock
    private OllamaEmbeddingModel embeddingModel;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private JwtService jwtService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Spy
    private MeterRegistry meterRegistry = new SimpleMeterRegistry();

    @InjectMocks
    private MatchServiceImpl matchService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(matchService, "temp", 0.3);
    }

    @Test
    @DisplayName("calculateMatchScore returns score when valid resume and prompt response")
    void calculateMatchScore_Success() {
        MatchScoreRequest request = new MatchScoreRequest();
        request.setResumeId(1L);
        request.setJobDescription("Java Spring Boot Developer");

        when(userServiceClient.getResumeText(1L, "Bearer token")).thenReturn("Experienced Java Developer");

        float[] mockVec = new float[]{1.0f, 0.5f};
        when(embeddingModel.embed("Experienced Java Developer")).thenReturn(mockVec);
        when(embeddingModel.embed("Java Spring Boot Developer")).thenReturn(mockVec);

        String mockLlmJson = """
        {
            "score": 85.0,
            "explanation": "Great fit for backend",
            "missingSkills": ["Docker"]
        }
        """;
        when(chatModel.call(anyString())).thenReturn(mockLlmJson);

        MatchScoreResponse response = matchService.calculateMatchScore(request, "Bearer token");

        assertNotNull(response);
        assertEquals(100.0, response.getScore());
        assertEquals("Great fit for backend", response.getExplanation());
        assertEquals(List.of("Docker"), response.getMissingSkills());
    }

    @Test
    @DisplayName("calculateMatchScore returns fallback when resumeText is null")
    void calculateMatchScore_ResumeNotFound() {
        MatchScoreRequest request = new MatchScoreRequest();
        request.setResumeId(1L);
        request.setJobDescription("Python Dev");

        when(userServiceClient.getResumeText(1L, "Bearer token")).thenReturn(null);

        MatchScoreResponse response = matchService.calculateMatchScore(request, "Bearer token");

        assertNotNull(response);
        assertEquals(0.0, response.getScore());
        assertEquals("Could not retrieve resume for analysis", response.getExplanation());
        verify(chatModel, never()).call(anyString());
    }

    @Test
    @DisplayName("generateTailoredResume succeeds and saves generated resume")
    void generateTailoredResume_Success() {
        ResumeGenerationRequest request = new ResumeGenerationRequest();
        request.setResumeId(1L);
        request.setCompanyName("Google");
        request.setJobTitle("Backend Lead");
        request.setJobDescription("Requires 5+ years of Java");

        when(userServiceClient.getResumeText(1L, "Bearer token")).thenReturn("5 years Java experience");

        String mockTailoredJson = """
        {
            "tailoredResume": "Lead Engineer Resume Text",
            "coverLetter": "Dear Google Recruiter...",
            "keyChanges": ["Refined achievements", "Highlighted microservices"]
        }
        """;

        ChatResponse mockChatResponse = mock(ChatResponse.class);
        Generation mockGeneration = mock(Generation.class);
        AssistantMessage mockOutput = mock(AssistantMessage.class);

        when(chatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);
        when(mockChatResponse.getResult()).thenReturn(mockGeneration);
        when(mockGeneration.getOutput()).thenReturn(mockOutput);
        when(mockOutput.getText()).thenReturn(mockTailoredJson);

        when(jwtService.extractUserId(anyString())).thenReturn(10L);
        when(userServiceClient.saveGeneratedResume(any(SaveGeneratedResumeRequest.class), eq("Bearer token")))
                .thenReturn(99L);

        ResumeGenerationResponse response = matchService.generateTailoredResume(request, "Bearer token");

        assertNotNull(response);
        assertEquals("Lead Engineer Resume Text", response.getTailoredResume());
        assertEquals(99L, response.getSavedResumeId());
        assertNotNull(response.getGeneratedAt());
    }

    @Test
    @DisplayName("generateTailoredResume throws ResumeContentNotFoundException when resume text is blank")
    void generateTailoredResume_ThrowsException_WhenResumeEmpty() {
        ResumeGenerationRequest request = new ResumeGenerationRequest();
        request.setResumeId(1L);

        when(userServiceClient.getResumeText(1L, "Bearer token")).thenReturn("   ");

        assertThrows(ResumeContentNotFoundException.class, () ->
                matchService.generateTailoredResume(request, "Bearer token"));
    }
}