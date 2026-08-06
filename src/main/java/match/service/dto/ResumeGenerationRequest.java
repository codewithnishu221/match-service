package match.service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ResumeGenerationRequest {
    @NotNull
    private Long resumeId;
    @NotNull
    private String jobDescription;
    private String jobTitle;
    private String companyName;
}
