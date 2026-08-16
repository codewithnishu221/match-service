package match.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumeGenerationRequest {
    @NotNull
    private Long resumeId;
    @NotBlank
    private String jobDescription;
    private String jobTitle;
    private String companyName;
}
