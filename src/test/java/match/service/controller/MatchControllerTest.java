package match.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import match.service.dto.MatchScoreRequest;
import match.service.dto.MatchScoreResponse;
import match.service.dto.ResumeGenerationRequest;
import match.service.dto.ResumeGenerationResponse;
import match.service.service.JwtService;
import match.service.service.MatchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class MatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MatchService matchService;

    @MockBean
    private JwtService jwtService;

    @Test
    @DisplayName("POST /api/match/score - 200 OK")
    void createScore_Success() throws Exception {
        MatchScoreRequest request = new MatchScoreRequest();
        request.setResumeId(1L);
        request.setJobDescription("Looking for Java Developer");

        MatchScoreResponse response = new MatchScoreResponse(85.0, "Good match", List.of("Docker"));
        when(matchService.calculateMatchScore(any(MatchScoreRequest.class), anyString()))
                .thenReturn(response);

        mockMvc.perform(post("/api/match/score")
                        .header("Authorization", "Bearer mock-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(85.0))
                .andExpect(jsonPath("$.explanation").value("Good match"));
    }

    @Test
    @DisplayName("POST /api/match/generate-resume - 200 OK")
    void generateResume_Success() throws Exception {
        ResumeGenerationRequest request = new ResumeGenerationRequest();
        request.setResumeId(1L);
        request.setCompanyName("Acme Corp");
        request.setJobTitle("Backend Dev");
        request.setJobDescription("Java Spring Boot Developer Needed");

        ResumeGenerationResponse response = new ResumeGenerationResponse();
        response.setTailoredResume("Optimized resume content");
        response.setCoverLetter("Cover letter content");
        response.setSavedResumeId(123L);

        when(matchService.generateTailoredResume(any(ResumeGenerationRequest.class), anyString()))
                .thenReturn(response);

        mockMvc.perform(post("/api/match/generate-resume")
                        .header("Authorization", "Bearer mock-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tailoredResume").value("Optimized resume content"))
                .andExpect(jsonPath("$.savedResumeId").value(123));
    }
}