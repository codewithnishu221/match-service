package match.service.client;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@AllArgsConstructor
@Slf4j
public class UserServiceClient {
    private final RestClient restClient;

    public String getResumeText( Long resumeId,  String token) {
        try {
            String resumeText = restClient.get()
                    .uri("/api/resumes/" + resumeId)
                    .header("Authorization", token)
                    .retrieve()
                    .body(String.class);
            return resumeText;
        } catch (Exception e) {
            log.error("Failed to fetch resume text for resumeId: {}", resumeId, e);
            return null;
        }
    }
}
