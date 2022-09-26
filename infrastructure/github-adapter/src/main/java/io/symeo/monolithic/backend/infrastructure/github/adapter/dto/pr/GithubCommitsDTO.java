
package io.symeo.monolithic.backend.infrastructure.github.adapter.dto.pr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GithubCommitsDTO {
    public String sha;
    public GithubCommitDTO commit;
    public List<GithubCommitParentDTO> parents;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class GithubCommitDTO {
        public GithubCommitterDTO committer;
        public String message;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class GithubCommitterDTO {
        public String name;
        public Date date;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class GithubCommitParentDTO {
        public String sha;
    }

}
