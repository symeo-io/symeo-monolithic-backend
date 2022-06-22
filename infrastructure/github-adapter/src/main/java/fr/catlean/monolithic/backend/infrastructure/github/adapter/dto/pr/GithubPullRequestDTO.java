
package fr.catlean.monolithic.backend.infrastructure.github.adapter.dto.pr;

import java.util.*;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "url",
    "id",
    "node_id",
    "html_url",
    "diff_url",
    "patch_url",
    "issue_url",
    "number",
    "state",
    "locked",
    "title",
    "user",
    "body",
    "created_at",
    "updated_at",
    "closed_at",
    "merged_at",
    "merge_commit_sha",
    "assignee",
    "assignees",
    "requested_reviewers",
    "requested_teams",
    "labels",
    "milestone",
    "draft",
    "commits_url",
    "review_comments_url",
    "review_comment_url",
    "comments_url",
    "statuses_url",
    "head",
    "base",
    "_links",
    "author_association",
    "auto_merge",
    "active_lock_reason",
    "merged",
    "mergeable",
    "rebaseable",
    "mergeable_state",
    "merged_by",
    "comments",
    "review_comments",
    "maintainer_can_modify",
    "commits",
    "additions",
    "deletions",
    "changed_files"
})
@Generated("jsonschema2pojo")
public class GithubPullRequestDTO {

    @JsonProperty("url")
    private String url;
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("node_id")
    private String nodeId;
    @JsonProperty("html_url")
    private String htmlUrl;
    @JsonProperty("diff_url")
    private String diffUrl;
    @JsonProperty("patch_url")
    private String patchUrl;
    @JsonProperty("issue_url")
    private String issueUrl;
    @JsonProperty("number")
    private Integer number;
    @JsonProperty("state")
    private String state;
    @JsonProperty("locked")
    private Boolean locked;
    @JsonProperty("title")
    private String title;
    @JsonProperty("user")
    private GithubUserDTO user;
    @JsonProperty("body")
    private Object body;
    @JsonProperty("created_at")
    private Date createdAt;
    @JsonProperty("updated_at")
    private Date updatedAt;
    @JsonProperty("closed_at")
    private Date closedAt;
    @JsonProperty("merged_at")
    private Date mergedAt;
    @JsonProperty("merge_commit_sha")
    private String mergeCommitSha;
    @JsonProperty("assignee")
    private Object assignee;
    @JsonProperty("assignees")
    private List<Object> assignees = new ArrayList<Object>();
    @JsonProperty("requested_reviewers")
    private List<Object> requestedReviewers = new ArrayList<Object>();
    @JsonProperty("requested_teams")
    private List<Object> requestedTeams = new ArrayList<Object>();
    @JsonProperty("labels")
    private List<Object> labels = new ArrayList<Object>();
    @JsonProperty("milestone")
    private Object milestone;
    @JsonProperty("draft")
    private Boolean draft;
    @JsonProperty("commits_url")
    private String commitsUrl;
    @JsonProperty("review_comments_url")
    private String reviewCommentsUrl;
    @JsonProperty("review_comment_url")
    private String reviewCommentUrl;
    @JsonProperty("comments_url")
    private String commentsUrl;
    @JsonProperty("statuses_url")
    private String statusesUrl;
    @JsonProperty("head")
    private GithubHeadDTO head;
    @JsonProperty("base")
    private GithubBaseDTO base;
    @JsonProperty("_links")
    private GithubLinksDTO links;
    @JsonProperty("author_association")
    private String authorAssociation;
    @JsonProperty("auto_merge")
    private Object autoMerge;
    @JsonProperty("active_lock_reason")
    private Object activeLockReason;
    @JsonProperty("merged")
    private Boolean merged;
    @JsonProperty("mergeable")
    private Boolean mergeable;
    @JsonProperty("rebaseable")
    private Boolean rebaseable;
    @JsonProperty("mergeable_state")
    private String mergeableState;
    @JsonProperty("merged_by")
    private Object mergedBy;
    @JsonProperty("comments")
    private Integer comments;
    @JsonProperty("review_comments")
    private Integer reviewComments;
    @JsonProperty("maintainer_can_modify")
    private Boolean maintainerCanModify;
    @JsonProperty("commits")
    private Integer commits;
    @JsonProperty("additions")
    private Integer additions;
    @JsonProperty("deletions")
    private Integer deletions;
    @JsonProperty("changed_files")
    private Integer changedFiles;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    public GithubPullRequestDTO withUrl(String url) {
        this.url = url;
        return this;
    }

    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

    public GithubPullRequestDTO withId(Integer id) {
        this.id = id;
        return this;
    }

    @JsonProperty("node_id")
    public String getNodeId() {
        return nodeId;
    }

    @JsonProperty("node_id")
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public GithubPullRequestDTO withNodeId(String nodeId) {
        this.nodeId = nodeId;
        return this;
    }

    @JsonProperty("html_url")
    public String getHtmlUrl() {
        return htmlUrl;
    }

    @JsonProperty("html_url")
    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public GithubPullRequestDTO withHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
        return this;
    }

    @JsonProperty("diff_url")
    public String getDiffUrl() {
        return diffUrl;
    }

    @JsonProperty("diff_url")
    public void setDiffUrl(String diffUrl) {
        this.diffUrl = diffUrl;
    }

    public GithubPullRequestDTO withDiffUrl(String diffUrl) {
        this.diffUrl = diffUrl;
        return this;
    }

    @JsonProperty("patch_url")
    public String getPatchUrl() {
        return patchUrl;
    }

    @JsonProperty("patch_url")
    public void setPatchUrl(String patchUrl) {
        this.patchUrl = patchUrl;
    }

    public GithubPullRequestDTO withPatchUrl(String patchUrl) {
        this.patchUrl = patchUrl;
        return this;
    }

    @JsonProperty("issue_url")
    public String getIssueUrl() {
        return issueUrl;
    }

    @JsonProperty("issue_url")
    public void setIssueUrl(String issueUrl) {
        this.issueUrl = issueUrl;
    }

    public GithubPullRequestDTO withIssueUrl(String issueUrl) {
        this.issueUrl = issueUrl;
        return this;
    }

    @JsonProperty("number")
    public Integer getNumber() {
        return number;
    }

    @JsonProperty("number")
    public void setNumber(Integer number) {
        this.number = number;
    }

    public GithubPullRequestDTO withNumber(Integer number) {
        this.number = number;
        return this;
    }

    @JsonProperty("state")
    public String getState() {
        return state;
    }

    @JsonProperty("state")
    public void setState(String state) {
        this.state = state;
    }

    public GithubPullRequestDTO withState(String state) {
        this.state = state;
        return this;
    }

    @JsonProperty("locked")
    public Boolean getLocked() {
        return locked;
    }

    @JsonProperty("locked")
    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public GithubPullRequestDTO withLocked(Boolean locked) {
        this.locked = locked;
        return this;
    }

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    public GithubPullRequestDTO withTitle(String title) {
        this.title = title;
        return this;
    }

    @JsonProperty("user")
    public GithubUserDTO getUser() {
        return user;
    }

    @JsonProperty("user")
    public void setUser(GithubUserDTO user) {
        this.user = user;
    }

    public GithubPullRequestDTO withUser(GithubUserDTO user) {
        this.user = user;
        return this;
    }

    @JsonProperty("body")
    public Object getBody() {
        return body;
    }

    @JsonProperty("body")
    public void setBody(Object body) {
        this.body = body;
    }

    public GithubPullRequestDTO withBody(Object body) {
        this.body = body;
        return this;
    }

    @JsonProperty("created_at")
    public Date getCreatedAt() {
        return createdAt;
    }

    @JsonProperty("created_at")
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public GithubPullRequestDTO withCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    @JsonProperty("updated_at")
    public Date getUpdatedAt() {
        return updatedAt;
    }

    @JsonProperty("updated_at")
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public GithubPullRequestDTO withUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    @JsonProperty("closed_at")
    public Date getClosedAt() {
        return closedAt;
    }

    @JsonProperty("closed_at")
    public void setClosedAt(Date closedAt) {
        this.closedAt = closedAt;
    }

    public GithubPullRequestDTO withClosedAt(Date closedAt) {
        this.closedAt = closedAt;
        return this;
    }

    @JsonProperty("merged_at")
    public Date getMergedAt() {
        return mergedAt;
    }

    @JsonProperty("merged_at")
    public void setMergedAt(Date mergedAt) {
        this.mergedAt = mergedAt;
    }

    public GithubPullRequestDTO withMergedAt(Date mergedAt) {
        this.mergedAt = mergedAt;
        return this;
    }

    @JsonProperty("merge_commit_sha")
    public String getMergeCommitSha() {
        return mergeCommitSha;
    }

    @JsonProperty("merge_commit_sha")
    public void setMergeCommitSha(String mergeCommitSha) {
        this.mergeCommitSha = mergeCommitSha;
    }

    public GithubPullRequestDTO withMergeCommitSha(String mergeCommitSha) {
        this.mergeCommitSha = mergeCommitSha;
        return this;
    }

    @JsonProperty("assignee")
    public Object getAssignee() {
        return assignee;
    }

    @JsonProperty("assignee")
    public void setAssignee(Object assignee) {
        this.assignee = assignee;
    }

    public GithubPullRequestDTO withAssignee(Object assignee) {
        this.assignee = assignee;
        return this;
    }

    @JsonProperty("assignees")
    public List<Object> getAssignees() {
        return assignees;
    }

    @JsonProperty("assignees")
    public void setAssignees(List<Object> assignees) {
        this.assignees = assignees;
    }

    public GithubPullRequestDTO withAssignees(List<Object> assignees) {
        this.assignees = assignees;
        return this;
    }

    @JsonProperty("requested_reviewers")
    public List<Object> getRequestedReviewers() {
        return requestedReviewers;
    }

    @JsonProperty("requested_reviewers")
    public void setRequestedReviewers(List<Object> requestedReviewers) {
        this.requestedReviewers = requestedReviewers;
    }

    public GithubPullRequestDTO withRequestedReviewers(List<Object> requestedReviewers) {
        this.requestedReviewers = requestedReviewers;
        return this;
    }

    @JsonProperty("requested_teams")
    public List<Object> getRequestedTeams() {
        return requestedTeams;
    }

    @JsonProperty("requested_teams")
    public void setRequestedTeams(List<Object> requestedTeams) {
        this.requestedTeams = requestedTeams;
    }

    public GithubPullRequestDTO withRequestedTeams(List<Object> requestedTeams) {
        this.requestedTeams = requestedTeams;
        return this;
    }

    @JsonProperty("labels")
    public List<Object> getLabels() {
        return labels;
    }

    @JsonProperty("labels")
    public void setLabels(List<Object> labels) {
        this.labels = labels;
    }

    public GithubPullRequestDTO withLabels(List<Object> labels) {
        this.labels = labels;
        return this;
    }

    @JsonProperty("milestone")
    public Object getMilestone() {
        return milestone;
    }

    @JsonProperty("milestone")
    public void setMilestone(Object milestone) {
        this.milestone = milestone;
    }

    public GithubPullRequestDTO withMilestone(Object milestone) {
        this.milestone = milestone;
        return this;
    }

    @JsonProperty("draft")
    public Boolean getDraft() {
        return draft;
    }

    @JsonProperty("draft")
    public void setDraft(Boolean draft) {
        this.draft = draft;
    }

    public GithubPullRequestDTO withDraft(Boolean draft) {
        this.draft = draft;
        return this;
    }

    @JsonProperty("commits_url")
    public String getCommitsUrl() {
        return commitsUrl;
    }

    @JsonProperty("commits_url")
    public void setCommitsUrl(String commitsUrl) {
        this.commitsUrl = commitsUrl;
    }

    public GithubPullRequestDTO withCommitsUrl(String commitsUrl) {
        this.commitsUrl = commitsUrl;
        return this;
    }

    @JsonProperty("review_comments_url")
    public String getReviewCommentsUrl() {
        return reviewCommentsUrl;
    }

    @JsonProperty("review_comments_url")
    public void setReviewCommentsUrl(String reviewCommentsUrl) {
        this.reviewCommentsUrl = reviewCommentsUrl;
    }

    public GithubPullRequestDTO withReviewCommentsUrl(String reviewCommentsUrl) {
        this.reviewCommentsUrl = reviewCommentsUrl;
        return this;
    }

    @JsonProperty("review_comment_url")
    public String getReviewCommentUrl() {
        return reviewCommentUrl;
    }

    @JsonProperty("review_comment_url")
    public void setReviewCommentUrl(String reviewCommentUrl) {
        this.reviewCommentUrl = reviewCommentUrl;
    }

    public GithubPullRequestDTO withReviewCommentUrl(String reviewCommentUrl) {
        this.reviewCommentUrl = reviewCommentUrl;
        return this;
    }

    @JsonProperty("comments_url")
    public String getCommentsUrl() {
        return commentsUrl;
    }

    @JsonProperty("comments_url")
    public void setCommentsUrl(String commentsUrl) {
        this.commentsUrl = commentsUrl;
    }

    public GithubPullRequestDTO withCommentsUrl(String commentsUrl) {
        this.commentsUrl = commentsUrl;
        return this;
    }

    @JsonProperty("statuses_url")
    public String getStatusesUrl() {
        return statusesUrl;
    }

    @JsonProperty("statuses_url")
    public void setStatusesUrl(String statusesUrl) {
        this.statusesUrl = statusesUrl;
    }

    public GithubPullRequestDTO withStatusesUrl(String statusesUrl) {
        this.statusesUrl = statusesUrl;
        return this;
    }

    @JsonProperty("head")
    public GithubHeadDTO getHead() {
        return head;
    }

    @JsonProperty("head")
    public void setHead(GithubHeadDTO head) {
        this.head = head;
    }

    public GithubPullRequestDTO withHead(GithubHeadDTO head) {
        this.head = head;
        return this;
    }

    @JsonProperty("base")
    public GithubBaseDTO getBase() {
        return base;
    }

    @JsonProperty("base")
    public void setBase(GithubBaseDTO base) {
        this.base = base;
    }

    public GithubPullRequestDTO withBase(GithubBaseDTO base) {
        this.base = base;
        return this;
    }

    @JsonProperty("_links")
    public GithubLinksDTO getLinks() {
        return links;
    }

    @JsonProperty("_links")
    public void setLinks(GithubLinksDTO links) {
        this.links = links;
    }

    public GithubPullRequestDTO withLinks(GithubLinksDTO links) {
        this.links = links;
        return this;
    }

    @JsonProperty("author_association")
    public String getAuthorAssociation() {
        return authorAssociation;
    }

    @JsonProperty("author_association")
    public void setAuthorAssociation(String authorAssociation) {
        this.authorAssociation = authorAssociation;
    }

    public GithubPullRequestDTO withAuthorAssociation(String authorAssociation) {
        this.authorAssociation = authorAssociation;
        return this;
    }

    @JsonProperty("auto_merge")
    public Object getAutoMerge() {
        return autoMerge;
    }

    @JsonProperty("auto_merge")
    public void setAutoMerge(Object autoMerge) {
        this.autoMerge = autoMerge;
    }

    public GithubPullRequestDTO withAutoMerge(Object autoMerge) {
        this.autoMerge = autoMerge;
        return this;
    }

    @JsonProperty("active_lock_reason")
    public Object getActiveLockReason() {
        return activeLockReason;
    }

    @JsonProperty("active_lock_reason")
    public void setActiveLockReason(Object activeLockReason) {
        this.activeLockReason = activeLockReason;
    }

    public GithubPullRequestDTO withActiveLockReason(Object activeLockReason) {
        this.activeLockReason = activeLockReason;
        return this;
    }

    @JsonProperty("merged")
    public Boolean getMerged() {
        return merged;
    }

    @JsonProperty("merged")
    public void setMerged(Boolean merged) {
        this.merged = merged;
    }

    public GithubPullRequestDTO withMerged(Boolean merged) {
        this.merged = merged;
        return this;
    }

    @JsonProperty("mergeable")
    public Boolean getMergeable() {
        return mergeable;
    }

    @JsonProperty("mergeable")
    public void setMergeable(Boolean mergeable) {
        this.mergeable = mergeable;
    }

    public GithubPullRequestDTO withMergeable(Boolean mergeable) {
        this.mergeable = mergeable;
        return this;
    }

    @JsonProperty("rebaseable")
    public Boolean getRebaseable() {
        return rebaseable;
    }

    @JsonProperty("rebaseable")
    public void setRebaseable(Boolean rebaseable) {
        this.rebaseable = rebaseable;
    }

    public GithubPullRequestDTO withRebaseable(Boolean rebaseable) {
        this.rebaseable = rebaseable;
        return this;
    }

    @JsonProperty("mergeable_state")
    public String getMergeableState() {
        return mergeableState;
    }

    @JsonProperty("mergeable_state")
    public void setMergeableState(String mergeableState) {
        this.mergeableState = mergeableState;
    }

    public GithubPullRequestDTO withMergeableState(String mergeableState) {
        this.mergeableState = mergeableState;
        return this;
    }

    @JsonProperty("merged_by")
    public Object getMergedBy() {
        return mergedBy;
    }

    @JsonProperty("merged_by")
    public void setMergedBy(Object mergedBy) {
        this.mergedBy = mergedBy;
    }

    public GithubPullRequestDTO withMergedBy(Object mergedBy) {
        this.mergedBy = mergedBy;
        return this;
    }

    @JsonProperty("comments")
    public Integer getComments() {
        return comments;
    }

    @JsonProperty("comments")
    public void setComments(Integer comments) {
        this.comments = comments;
    }

    public GithubPullRequestDTO withComments(Integer comments) {
        this.comments = comments;
        return this;
    }

    @JsonProperty("review_comments")
    public Integer getReviewComments() {
        return reviewComments;
    }

    @JsonProperty("review_comments")
    public void setReviewComments(Integer reviewComments) {
        this.reviewComments = reviewComments;
    }

    public GithubPullRequestDTO withReviewComments(Integer reviewComments) {
        this.reviewComments = reviewComments;
        return this;
    }

    @JsonProperty("maintainer_can_modify")
    public Boolean getMaintainerCanModify() {
        return maintainerCanModify;
    }

    @JsonProperty("maintainer_can_modify")
    public void setMaintainerCanModify(Boolean maintainerCanModify) {
        this.maintainerCanModify = maintainerCanModify;
    }

    public GithubPullRequestDTO withMaintainerCanModify(Boolean maintainerCanModify) {
        this.maintainerCanModify = maintainerCanModify;
        return this;
    }

    @JsonProperty("commits")
    public Integer getCommits() {
        return commits;
    }

    @JsonProperty("commits")
    public void setCommits(Integer commits) {
        this.commits = commits;
    }

    public GithubPullRequestDTO withCommits(Integer commits) {
        this.commits = commits;
        return this;
    }

    @JsonProperty("additions")
    public Integer getAdditions() {
        return additions;
    }

    @JsonProperty("additions")
    public void setAdditions(Integer additions) {
        this.additions = additions;
    }

    public GithubPullRequestDTO withAdditions(Integer additions) {
        this.additions = additions;
        return this;
    }

    @JsonProperty("deletions")
    public Integer getDeletions() {
        return deletions;
    }

    @JsonProperty("deletions")
    public void setDeletions(Integer deletions) {
        this.deletions = deletions;
    }

    public GithubPullRequestDTO withDeletions(Integer deletions) {
        this.deletions = deletions;
        return this;
    }

    @JsonProperty("changed_files")
    public Integer getChangedFiles() {
        return changedFiles;
    }

    @JsonProperty("changed_files")
    public void setChangedFiles(Integer changedFiles) {
        this.changedFiles = changedFiles;
    }

    public GithubPullRequestDTO withChangedFiles(Integer changedFiles) {
        this.changedFiles = changedFiles;
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

    public GithubPullRequestDTO withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(GithubPullRequestDTO.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("url");
        sb.append('=');
        sb.append(((this.url == null)?"<null>":this.url));
        sb.append(',');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null)?"<null>":this.id));
        sb.append(',');
        sb.append("nodeId");
        sb.append('=');
        sb.append(((this.nodeId == null)?"<null>":this.nodeId));
        sb.append(',');
        sb.append("htmlUrl");
        sb.append('=');
        sb.append(((this.htmlUrl == null)?"<null>":this.htmlUrl));
        sb.append(',');
        sb.append("diffUrl");
        sb.append('=');
        sb.append(((this.diffUrl == null)?"<null>":this.diffUrl));
        sb.append(',');
        sb.append("patchUrl");
        sb.append('=');
        sb.append(((this.patchUrl == null)?"<null>":this.patchUrl));
        sb.append(',');
        sb.append("issueUrl");
        sb.append('=');
        sb.append(((this.issueUrl == null)?"<null>":this.issueUrl));
        sb.append(',');
        sb.append("number");
        sb.append('=');
        sb.append(((this.number == null)?"<null>":this.number));
        sb.append(',');
        sb.append("state");
        sb.append('=');
        sb.append(((this.state == null)?"<null>":this.state));
        sb.append(',');
        sb.append("locked");
        sb.append('=');
        sb.append(((this.locked == null)?"<null>":this.locked));
        sb.append(',');
        sb.append("title");
        sb.append('=');
        sb.append(((this.title == null)?"<null>":this.title));
        sb.append(',');
        sb.append("user");
        sb.append('=');
        sb.append(((this.user == null)?"<null>":this.user));
        sb.append(',');
        sb.append("body");
        sb.append('=');
        sb.append(((this.body == null)?"<null>":this.body));
        sb.append(',');
        sb.append("createdAt");
        sb.append('=');
        sb.append(((this.createdAt == null)?"<null>":this.createdAt));
        sb.append(',');
        sb.append("updatedAt");
        sb.append('=');
        sb.append(((this.updatedAt == null)?"<null>":this.updatedAt));
        sb.append(',');
        sb.append("closedAt");
        sb.append('=');
        sb.append(((this.closedAt == null)?"<null>":this.closedAt));
        sb.append(',');
        sb.append("mergedAt");
        sb.append('=');
        sb.append(((this.mergedAt == null)?"<null>":this.mergedAt));
        sb.append(',');
        sb.append("mergeCommitSha");
        sb.append('=');
        sb.append(((this.mergeCommitSha == null)?"<null>":this.mergeCommitSha));
        sb.append(',');
        sb.append("assignee");
        sb.append('=');
        sb.append(((this.assignee == null)?"<null>":this.assignee));
        sb.append(',');
        sb.append("assignees");
        sb.append('=');
        sb.append(((this.assignees == null)?"<null>":this.assignees));
        sb.append(',');
        sb.append("requestedReviewers");
        sb.append('=');
        sb.append(((this.requestedReviewers == null)?"<null>":this.requestedReviewers));
        sb.append(',');
        sb.append("requestedTeams");
        sb.append('=');
        sb.append(((this.requestedTeams == null)?"<null>":this.requestedTeams));
        sb.append(',');
        sb.append("labels");
        sb.append('=');
        sb.append(((this.labels == null)?"<null>":this.labels));
        sb.append(',');
        sb.append("milestone");
        sb.append('=');
        sb.append(((this.milestone == null)?"<null>":this.milestone));
        sb.append(',');
        sb.append("draft");
        sb.append('=');
        sb.append(((this.draft == null)?"<null>":this.draft));
        sb.append(',');
        sb.append("commitsUrl");
        sb.append('=');
        sb.append(((this.commitsUrl == null)?"<null>":this.commitsUrl));
        sb.append(',');
        sb.append("reviewCommentsUrl");
        sb.append('=');
        sb.append(((this.reviewCommentsUrl == null)?"<null>":this.reviewCommentsUrl));
        sb.append(',');
        sb.append("reviewCommentUrl");
        sb.append('=');
        sb.append(((this.reviewCommentUrl == null)?"<null>":this.reviewCommentUrl));
        sb.append(',');
        sb.append("commentsUrl");
        sb.append('=');
        sb.append(((this.commentsUrl == null)?"<null>":this.commentsUrl));
        sb.append(',');
        sb.append("statusesUrl");
        sb.append('=');
        sb.append(((this.statusesUrl == null)?"<null>":this.statusesUrl));
        sb.append(',');
        sb.append("head");
        sb.append('=');
        sb.append(((this.head == null)?"<null>":this.head));
        sb.append(',');
        sb.append("base");
        sb.append('=');
        sb.append(((this.base == null)?"<null>":this.base));
        sb.append(',');
        sb.append("links");
        sb.append('=');
        sb.append(((this.links == null)?"<null>":this.links));
        sb.append(',');
        sb.append("authorAssociation");
        sb.append('=');
        sb.append(((this.authorAssociation == null)?"<null>":this.authorAssociation));
        sb.append(',');
        sb.append("autoMerge");
        sb.append('=');
        sb.append(((this.autoMerge == null)?"<null>":this.autoMerge));
        sb.append(',');
        sb.append("activeLockReason");
        sb.append('=');
        sb.append(((this.activeLockReason == null)?"<null>":this.activeLockReason));
        sb.append(',');
        sb.append("merged");
        sb.append('=');
        sb.append(((this.merged == null)?"<null>":this.merged));
        sb.append(',');
        sb.append("mergeable");
        sb.append('=');
        sb.append(((this.mergeable == null)?"<null>":this.mergeable));
        sb.append(',');
        sb.append("rebaseable");
        sb.append('=');
        sb.append(((this.rebaseable == null)?"<null>":this.rebaseable));
        sb.append(',');
        sb.append("mergeableState");
        sb.append('=');
        sb.append(((this.mergeableState == null)?"<null>":this.mergeableState));
        sb.append(',');
        sb.append("mergedBy");
        sb.append('=');
        sb.append(((this.mergedBy == null)?"<null>":this.mergedBy));
        sb.append(',');
        sb.append("comments");
        sb.append('=');
        sb.append(((this.comments == null)?"<null>":this.comments));
        sb.append(',');
        sb.append("reviewComments");
        sb.append('=');
        sb.append(((this.reviewComments == null)?"<null>":this.reviewComments));
        sb.append(',');
        sb.append("maintainerCanModify");
        sb.append('=');
        sb.append(((this.maintainerCanModify == null)?"<null>":this.maintainerCanModify));
        sb.append(',');
        sb.append("commits");
        sb.append('=');
        sb.append(((this.commits == null)?"<null>":this.commits));
        sb.append(',');
        sb.append("additions");
        sb.append('=');
        sb.append(((this.additions == null)?"<null>":this.additions));
        sb.append(',');
        sb.append("deletions");
        sb.append('=');
        sb.append(((this.deletions == null)?"<null>":this.deletions));
        sb.append(',');
        sb.append("changedFiles");
        sb.append('=');
        sb.append(((this.changedFiles == null)?"<null>":this.changedFiles));
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
        result = ((result* 31)+((this.requestedReviewers == null)? 0 :this.requestedReviewers.hashCode()));
        result = ((result* 31)+((this.mergeableState == null)? 0 :this.mergeableState.hashCode()));
        result = ((result* 31)+((this.requestedTeams == null)? 0 :this.requestedTeams.hashCode()));
        result = ((result* 31)+((this.assignees == null)? 0 :this.assignees.hashCode()));
        result = ((result* 31)+((this.reviewCommentsUrl == null)? 0 :this.reviewCommentsUrl.hashCode()));
        result = ((result* 31)+((this.body == null)? 0 :this.body.hashCode()));
        result = ((result* 31)+((this.commitsUrl == null)? 0 :this.commitsUrl.hashCode()));
        result = ((result* 31)+((this.reviewComments == null)? 0 :this.reviewComments.hashCode()));
        result = ((result* 31)+((this.number == null)? 0 :this.number.hashCode()));
        result = ((result* 31)+((this.createdAt == null)? 0 :this.createdAt.hashCode()));
        result = ((result* 31)+((this.mergeable == null)? 0 :this.mergeable.hashCode()));
        result = ((result* 31)+((this.draft == null)? 0 :this.draft.hashCode()));
        result = ((result* 31)+((this.activeLockReason == null)? 0 :this.activeLockReason.hashCode()));
        result = ((result* 31)+((this.statusesUrl == null)? 0 :this.statusesUrl.hashCode()));
        result = ((result* 31)+((this.links == null)? 0 :this.links.hashCode()));
        result = ((result* 31)+((this.id == null)? 0 :this.id.hashCode()));
        result = ((result* 31)+((this.state == null)? 0 :this.state.hashCode()));
        result = ((result* 31)+((this.locked == null)? 0 :this.locked.hashCode()));
        result = ((result* 31)+((this.closedAt == null)? 0 :this.closedAt.hashCode()));
        result = ((result* 31)+((this.updatedAt == null)? 0 :this.updatedAt.hashCode()));
        result = ((result* 31)+((this.labels == null)? 0 :this.labels.hashCode()));
        result = ((result* 31)+((this.commentsUrl == null)? 0 :this.commentsUrl.hashCode()));
        result = ((result* 31)+((this.commits == null)? 0 :this.commits.hashCode()));
        result = ((result* 31)+((this.assignee == null)? 0 :this.assignee.hashCode()));
        result = ((result* 31)+((this.mergedBy == null)? 0 :this.mergedBy.hashCode()));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        result = ((result* 31)+((this.nodeId == null)? 0 :this.nodeId.hashCode()));
        result = ((result* 31)+((this.diffUrl == null)? 0 :this.diffUrl.hashCode()));
        result = ((result* 31)+((this.rebaseable == null)? 0 :this.rebaseable.hashCode()));
        result = ((result* 31)+((this.reviewCommentUrl == null)? 0 :this.reviewCommentUrl.hashCode()));
        result = ((result* 31)+((this.deletions == null)? 0 :this.deletions.hashCode()));
        result = ((result* 31)+((this.autoMerge == null)? 0 :this.autoMerge.hashCode()));
        result = ((result* 31)+((this.title == null)? 0 :this.title.hashCode()));
        result = ((result* 31)+((this.head == null)? 0 :this.head.hashCode()));
        result = ((result* 31)+((this.patchUrl == null)? 0 :this.patchUrl.hashCode()));
        result = ((result* 31)+((this.changedFiles == null)? 0 :this.changedFiles.hashCode()));
        result = ((result* 31)+((this.mergeCommitSha == null)? 0 :this.mergeCommitSha.hashCode()));
        result = ((result* 31)+((this.authorAssociation == null)? 0 :this.authorAssociation.hashCode()));
        result = ((result* 31)+((this.comments == null)? 0 :this.comments.hashCode()));
        result = ((result* 31)+((this.additions == null)? 0 :this.additions.hashCode()));
        result = ((result* 31)+((this.mergedAt == null)? 0 :this.mergedAt.hashCode()));
        result = ((result* 31)+((this.htmlUrl == null)? 0 :this.htmlUrl.hashCode()));
        result = ((result* 31)+((this.merged == null)? 0 :this.merged.hashCode()));
        result = ((result* 31)+((this.maintainerCanModify == null)? 0 :this.maintainerCanModify.hashCode()));
        result = ((result* 31)+((this.url == null)? 0 :this.url.hashCode()));
        result = ((result* 31)+((this.issueUrl == null)? 0 :this.issueUrl.hashCode()));
        result = ((result* 31)+((this.milestone == null)? 0 :this.milestone.hashCode()));
        result = ((result* 31)+((this.user == null)? 0 :this.user.hashCode()));
        result = ((result* 31)+((this.base == null)? 0 :this.base.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof GithubPullRequestDTO) == false) {
            return false;
        }
        GithubPullRequestDTO rhs = ((GithubPullRequestDTO) other);
        return ((((((((((((((((((((((((((((((((((((((((((((((((((this.requestedReviewers == rhs.requestedReviewers)||((this.requestedReviewers!= null)&&this.requestedReviewers.equals(rhs.requestedReviewers)))&&((this.mergeableState == rhs.mergeableState)||((this.mergeableState!= null)&&this.mergeableState.equals(rhs.mergeableState))))&&((this.requestedTeams == rhs.requestedTeams)||((this.requestedTeams!= null)&&this.requestedTeams.equals(rhs.requestedTeams))))&&((this.assignees == rhs.assignees)||((this.assignees!= null)&&this.assignees.equals(rhs.assignees))))&&((this.reviewCommentsUrl == rhs.reviewCommentsUrl)||((this.reviewCommentsUrl!= null)&&this.reviewCommentsUrl.equals(rhs.reviewCommentsUrl))))&&((this.body == rhs.body)||((this.body!= null)&&this.body.equals(rhs.body))))&&((this.commitsUrl == rhs.commitsUrl)||((this.commitsUrl!= null)&&this.commitsUrl.equals(rhs.commitsUrl))))&&((this.reviewComments == rhs.reviewComments)||((this.reviewComments!= null)&&this.reviewComments.equals(rhs.reviewComments))))&&((this.number == rhs.number)||((this.number!= null)&&this.number.equals(rhs.number))))&&((this.createdAt == rhs.createdAt)||((this.createdAt!= null)&&this.createdAt.equals(rhs.createdAt))))&&((this.mergeable == rhs.mergeable)||((this.mergeable!= null)&&this.mergeable.equals(rhs.mergeable))))&&((this.draft == rhs.draft)||((this.draft!= null)&&this.draft.equals(rhs.draft))))&&((this.activeLockReason == rhs.activeLockReason)||((this.activeLockReason!= null)&&this.activeLockReason.equals(rhs.activeLockReason))))&&((this.statusesUrl == rhs.statusesUrl)||((this.statusesUrl!= null)&&this.statusesUrl.equals(rhs.statusesUrl))))&&((this.links == rhs.links)||((this.links!= null)&&this.links.equals(rhs.links))))&&((this.id == rhs.id)||((this.id!= null)&&this.id.equals(rhs.id))))&&((this.state == rhs.state)||((this.state!= null)&&this.state.equals(rhs.state))))&&((this.locked == rhs.locked)||((this.locked!= null)&&this.locked.equals(rhs.locked))))&&((this.closedAt == rhs.closedAt)||((this.closedAt!= null)&&this.closedAt.equals(rhs.closedAt))))&&((this.updatedAt == rhs.updatedAt)||((this.updatedAt!= null)&&this.updatedAt.equals(rhs.updatedAt))))&&((this.labels == rhs.labels)||((this.labels!= null)&&this.labels.equals(rhs.labels))))&&((this.commentsUrl == rhs.commentsUrl)||((this.commentsUrl!= null)&&this.commentsUrl.equals(rhs.commentsUrl))))&&((this.commits == rhs.commits)||((this.commits!= null)&&this.commits.equals(rhs.commits))))&&((this.assignee == rhs.assignee)||((this.assignee!= null)&&this.assignee.equals(rhs.assignee))))&&((this.mergedBy == rhs.mergedBy)||((this.mergedBy!= null)&&this.mergedBy.equals(rhs.mergedBy))))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))))&&((this.nodeId == rhs.nodeId)||((this.nodeId!= null)&&this.nodeId.equals(rhs.nodeId))))&&((this.diffUrl == rhs.diffUrl)||((this.diffUrl!= null)&&this.diffUrl.equals(rhs.diffUrl))))&&((this.rebaseable == rhs.rebaseable)||((this.rebaseable!= null)&&this.rebaseable.equals(rhs.rebaseable))))&&((this.reviewCommentUrl == rhs.reviewCommentUrl)||((this.reviewCommentUrl!= null)&&this.reviewCommentUrl.equals(rhs.reviewCommentUrl))))&&((this.deletions == rhs.deletions)||((this.deletions!= null)&&this.deletions.equals(rhs.deletions))))&&((this.autoMerge == rhs.autoMerge)||((this.autoMerge!= null)&&this.autoMerge.equals(rhs.autoMerge))))&&((this.title == rhs.title)||((this.title!= null)&&this.title.equals(rhs.title))))&&((this.head == rhs.head)||((this.head!= null)&&this.head.equals(rhs.head))))&&((this.patchUrl == rhs.patchUrl)||((this.patchUrl!= null)&&this.patchUrl.equals(rhs.patchUrl))))&&((this.changedFiles == rhs.changedFiles)||((this.changedFiles!= null)&&this.changedFiles.equals(rhs.changedFiles))))&&((this.mergeCommitSha == rhs.mergeCommitSha)||((this.mergeCommitSha!= null)&&this.mergeCommitSha.equals(rhs.mergeCommitSha))))&&((this.authorAssociation == rhs.authorAssociation)||((this.authorAssociation!= null)&&this.authorAssociation.equals(rhs.authorAssociation))))&&((this.comments == rhs.comments)||((this.comments!= null)&&this.comments.equals(rhs.comments))))&&((this.additions == rhs.additions)||((this.additions!= null)&&this.additions.equals(rhs.additions))))&&((this.mergedAt == rhs.mergedAt)||((this.mergedAt!= null)&&this.mergedAt.equals(rhs.mergedAt))))&&((this.htmlUrl == rhs.htmlUrl)||((this.htmlUrl!= null)&&this.htmlUrl.equals(rhs.htmlUrl))))&&((this.merged == rhs.merged)||((this.merged!= null)&&this.merged.equals(rhs.merged))))&&((this.maintainerCanModify == rhs.maintainerCanModify)||((this.maintainerCanModify!= null)&&this.maintainerCanModify.equals(rhs.maintainerCanModify))))&&((this.url == rhs.url)||((this.url!= null)&&this.url.equals(rhs.url))))&&((this.issueUrl == rhs.issueUrl)||((this.issueUrl!= null)&&this.issueUrl.equals(rhs.issueUrl))))&&((this.milestone == rhs.milestone)||((this.milestone!= null)&&this.milestone.equals(rhs.milestone))))&&((this.user == rhs.user)||((this.user!= null)&&this.user.equals(rhs.user))))&&((this.base == rhs.base)||((this.base!= null)&&this.base.equals(rhs.base))));
    }

}
