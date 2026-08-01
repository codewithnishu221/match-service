package match.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchScoreResponse {

    private Double score;
    private String explanation;
    private List<String> missingSkills;

}
