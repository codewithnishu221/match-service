package match.service.client;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import match.service.dto.ResumeGenerationRequest;
import match.service.dto.ResumeGenerationResponse;
import match.service.dto.SaveGeneratedResumeRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@AllArgsConstructor
@Slf4j
public class UserServiceClient {
    private final RestClient restClient;

    public String getResumeText( Long resumeId,  String token) {
        try {
            String resumeText = restClient.get()
                    .uri("/api/resumes/" + resumeId +"/text")
                    .header("Authorization", token)
                    .retrieve()
                    .body(String.class);
            return resumeText;
        } catch (Exception e) {
            log.error("Failed to fetch resume text for resumeId: {}", resumeId, e);
            return null;
        }
    }

    public Long saveGeneratedResume(SaveGeneratedResumeRequest saveRequest, String token) {
        try {
            var response = restClient.post()
                    .uri("/api/resumes/save-generated")
                    .header("Authorization", token)
                    .body(saveRequest)
                    .retrieve()
                    .body(Map.class);
            return response != null ? Long.valueOf(response.get("id").toString()) : null;

        } catch (Exception e) {
            log.error("Failed to save generated resume to User Service", e);
            return null;
        }

    }
}
