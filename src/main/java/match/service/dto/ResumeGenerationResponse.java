package match.service.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ResumeGenerationResponse {

    private String tailoredResume;
    private List<String> keyChanges;
    private String coverLetter;
    private LocalDateTime generatedAt;
}
