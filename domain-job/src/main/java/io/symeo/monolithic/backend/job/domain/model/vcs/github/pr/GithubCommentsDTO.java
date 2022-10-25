
package io.symeo.monolithic.backend.job.domain.model.vcs.github.pr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GithubCommentsDTO {

    @JsonProperty("created_at")
    Date creationDate;
    String id;

}
