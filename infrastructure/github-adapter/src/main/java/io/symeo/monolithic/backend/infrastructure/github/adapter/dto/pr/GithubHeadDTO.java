
package io.symeo.monolithic.backend.infrastructure.github.adapter.dto.pr;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.processing.Generated;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "label",
    "ref",
    "sha",
    "user",
    "repo"
})
@Generated("jsonschema2pojo")
public class GithubHeadDTO {

    @JsonProperty("label")
    private String label;
    @JsonProperty("ref")
    private String ref;
    @JsonProperty("sha")
    private String sha;
    @JsonProperty("user")
    private GithubUser1DTO user;
    @JsonProperty("repo")
    private GithubRepoDTO repo;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("label")
    public String getLabel() {
        return label;
    }

    @JsonProperty("label")
    public void setLabel(String label) {
        this.label = label;
    }

    public GithubHeadDTO withLabel(String label) {
        this.label = label;
        return this;
    }

    @JsonProperty("ref")
    public String getRef() {
        return ref;
    }

    @JsonProperty("ref")
    public void setRef(String ref) {
        this.ref = ref;
    }

    public GithubHeadDTO withRef(String ref) {
        this.ref = ref;
        return this;
    }

    @JsonProperty("sha")
    public String getSha() {
        return sha;
    }

    @JsonProperty("sha")
    public void setSha(String sha) {
        this.sha = sha;
    }

    public GithubHeadDTO withSha(String sha) {
        this.sha = sha;
        return this;
    }

    @JsonProperty("user")
    public GithubUser1DTO getUser() {
        return user;
    }

    @JsonProperty("user")
    public void setUser(GithubUser1DTO user) {
        this.user = user;
    }

    public GithubHeadDTO withUser(GithubUser1DTO user) {
        this.user = user;
        return this;
    }

    @JsonProperty("repo")
    public GithubRepoDTO getRepo() {
        return repo;
    }

    @JsonProperty("repo")
    public void setRepo(GithubRepoDTO repo) {
        this.repo = repo;
    }

    public GithubHeadDTO withRepo(GithubRepoDTO repo) {
        this.repo = repo;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public GithubHeadDTO withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(GithubHeadDTO.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("label");
        sb.append('=');
        sb.append(((this.label == null)?"<null>":this.label));
        sb.append(',');
        sb.append("ref");
        sb.append('=');
        sb.append(((this.ref == null)?"<null>":this.ref));
        sb.append(',');
        sb.append("sha");
        sb.append('=');
        sb.append(((this.sha == null)?"<null>":this.sha));
        sb.append(',');
        sb.append("user");
        sb.append('=');
        sb.append(((this.user == null)?"<null>":this.user));
        sb.append(',');
        sb.append("repo");
        sb.append('=');
        sb.append(((this.repo == null)?"<null>":this.repo));
        sb.append(',');
        sb.append("additionalProperties");
        sb.append('=');
        sb.append(((this.additionalProperties == null)?"<null>":this.additionalProperties));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.ref == null)? 0 :this.ref.hashCode()));
        result = ((result* 31)+((this.repo == null)? 0 :this.repo.hashCode()));
        result = ((result* 31)+((this.label == null)? 0 :this.label.hashCode()));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        result = ((result* 31)+((this.sha == null)? 0 :this.sha.hashCode()));
        result = ((result* 31)+((this.user == null)? 0 :this.user.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof GithubHeadDTO) == false) {
            return false;
        }
        GithubHeadDTO rhs = ((GithubHeadDTO) other);
        return (((((((this.ref == rhs.ref)||((this.ref!= null)&&this.ref.equals(rhs.ref)))&&((this.repo == rhs.repo)||((this.repo!= null)&&this.repo.equals(rhs.repo))))&&((this.label == rhs.label)||((this.label!= null)&&this.label.equals(rhs.label))))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))))&&((this.sha == rhs.sha)||((this.sha!= null)&&this.sha.equals(rhs.sha))))&&((this.user == rhs.user)||((this.user!= null)&&this.user.equals(rhs.user))));
    }

}
