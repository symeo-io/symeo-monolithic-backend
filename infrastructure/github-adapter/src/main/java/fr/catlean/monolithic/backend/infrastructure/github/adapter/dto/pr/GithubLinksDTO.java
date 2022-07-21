
package fr.catlean.monolithic.backend.infrastructure.github.adapter.dto.pr;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.processing.Generated;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "self",
    "html",
    "issue",
    "comments",
    "review_comments",
    "review_comment",
    "commits",
    "statuses"
})
@Generated("jsonschema2pojo")
public class GithubLinksDTO {

    @JsonProperty("self")
    private GithubSelfDTO self;
    @JsonProperty("html")
    private GithubHtmlDTO html;
    @JsonProperty("issue")
    private GithubIssueDTO issue;
    @JsonProperty("comments")
    private GithubCommentsDTO comments;
    @JsonProperty("review_comments")
    private GithubReviewCommentsDTO reviewComments;
    @JsonProperty("review_comment")
    private GithubReviewCommentDTO reviewComment;
    @JsonProperty("commits")
    private GithubCommitsDTO commits;
    @JsonProperty("statuses")
    private GithubStatusesDTO statuses;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("self")
    public GithubSelfDTO getSelf() {
        return self;
    }

    @JsonProperty("self")
    public void setSelf(GithubSelfDTO self) {
        this.self = self;
    }

    public GithubLinksDTO withSelf(GithubSelfDTO self) {
        this.self = self;
        return this;
    }

    @JsonProperty("html")
    public GithubHtmlDTO getHtml() {
        return html;
    }

    @JsonProperty("html")
    public void setHtml(GithubHtmlDTO html) {
        this.html = html;
    }

    public GithubLinksDTO withHtml(GithubHtmlDTO html) {
        this.html = html;
        return this;
    }

    @JsonProperty("issue")
    public GithubIssueDTO getIssue() {
        return issue;
    }

    @JsonProperty("issue")
    public void setIssue(GithubIssueDTO issue) {
        this.issue = issue;
    }

    public GithubLinksDTO withIssue(GithubIssueDTO issue) {
        this.issue = issue;
        return this;
    }

    @JsonProperty("comments")
    public GithubCommentsDTO getComments() {
        return comments;
    }

    @JsonProperty("comments")
    public void setComments(GithubCommentsDTO comments) {
        this.comments = comments;
    }

    public GithubLinksDTO withComments(GithubCommentsDTO comments) {
        this.comments = comments;
        return this;
    }

    @JsonProperty("review_comments")
    public GithubReviewCommentsDTO getReviewComments() {
        return reviewComments;
    }

    @JsonProperty("review_comments")
    public void setReviewComments(GithubReviewCommentsDTO reviewComments) {
        this.reviewComments = reviewComments;
    }

    public GithubLinksDTO withReviewComments(GithubReviewCommentsDTO reviewComments) {
        this.reviewComments = reviewComments;
        return this;
    }

    @JsonProperty("review_comment")
    public GithubReviewCommentDTO getReviewComment() {
        return reviewComment;
    }

    @JsonProperty("review_comment")
    public void setReviewComment(GithubReviewCommentDTO reviewComment) {
        this.reviewComment = reviewComment;
    }

    public GithubLinksDTO withReviewComment(GithubReviewCommentDTO reviewComment) {
        this.reviewComment = reviewComment;
        return this;
    }

    @JsonProperty("commits")
    public GithubCommitsDTO getCommits() {
        return commits;
    }

    @JsonProperty("commits")
    public void setCommits(GithubCommitsDTO commits) {
        this.commits = commits;
    }

    public GithubLinksDTO withCommits(GithubCommitsDTO commits) {
        this.commits = commits;
        return this;
    }

    @JsonProperty("statuses")
    public GithubStatusesDTO getStatuses() {
        return statuses;
    }

    @JsonProperty("statuses")
    public void setStatuses(GithubStatusesDTO statuses) {
        this.statuses = statuses;
    }

    public GithubLinksDTO withStatuses(GithubStatusesDTO statuses) {
        this.statuses = statuses;
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

    public GithubLinksDTO withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(GithubLinksDTO.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("self");
        sb.append('=');
        sb.append(((this.self == null)?"<null>":this.self));
        sb.append(',');
        sb.append("html");
        sb.append('=');
        sb.append(((this.html == null)?"<null>":this.html));
        sb.append(',');
        sb.append("issue");
        sb.append('=');
        sb.append(((this.issue == null)?"<null>":this.issue));
        sb.append(',');
        sb.append("comments");
        sb.append('=');
        sb.append(((this.comments == null)?"<null>":this.comments));
        sb.append(',');
        sb.append("reviewComments");
        sb.append('=');
        sb.append(((this.reviewComments == null)?"<null>":this.reviewComments));
        sb.append(',');
        sb.append("reviewComment");
        sb.append('=');
        sb.append(((this.reviewComment == null)?"<null>":this.reviewComment));
        sb.append(',');
        sb.append("commits");
        sb.append('=');
        sb.append(((this.commits == null)?"<null>":this.commits));
        sb.append(',');
        sb.append("statuses");
        sb.append('=');
        sb.append(((this.statuses == null)?"<null>":this.statuses));
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
        result = ((result* 31)+((this.comments == null)? 0 :this.comments.hashCode()));
        result = ((result* 31)+((this.issue == null)? 0 :this.issue.hashCode()));
        result = ((result* 31)+((this.reviewComment == null)? 0 :this.reviewComment.hashCode()));
        result = ((result* 31)+((this.self == null)? 0 :this.self.hashCode()));
        result = ((result* 31)+((this.commits == null)? 0 :this.commits.hashCode()));
        result = ((result* 31)+((this.statuses == null)? 0 :this.statuses.hashCode()));
        result = ((result* 31)+((this.html == null)? 0 :this.html.hashCode()));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        result = ((result* 31)+((this.reviewComments == null)? 0 :this.reviewComments.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof GithubLinksDTO) == false) {
            return false;
        }
        GithubLinksDTO rhs = ((GithubLinksDTO) other);
        return ((((((((((this.comments == rhs.comments)||((this.comments!= null)&&this.comments.equals(rhs.comments)))&&((this.issue == rhs.issue)||((this.issue!= null)&&this.issue.equals(rhs.issue))))&&((this.reviewComment == rhs.reviewComment)||((this.reviewComment!= null)&&this.reviewComment.equals(rhs.reviewComment))))&&((this.self == rhs.self)||((this.self!= null)&&this.self.equals(rhs.self))))&&((this.commits == rhs.commits)||((this.commits!= null)&&this.commits.equals(rhs.commits))))&&((this.statuses == rhs.statuses)||((this.statuses!= null)&&this.statuses.equals(rhs.statuses))))&&((this.html == rhs.html)||((this.html!= null)&&this.html.equals(rhs.html))))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))))&&((this.reviewComments == rhs.reviewComments)||((this.reviewComments!= null)&&this.reviewComments.equals(rhs.reviewComments))));
    }

}
