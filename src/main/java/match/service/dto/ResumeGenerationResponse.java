package match.service.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeGenerationResponse {

    private String tailoredResume;
    private List<String> keyChanges;
    private String coverLetter;
    private LocalDateTime generatedAt;
    private Long savedResumeId;
}
