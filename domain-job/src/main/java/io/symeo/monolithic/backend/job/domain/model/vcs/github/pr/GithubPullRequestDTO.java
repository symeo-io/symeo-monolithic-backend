
package io.symeo.monolithic.backend.job.domain.model.vcs.github.pr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
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
    @JsonProperty("github_commits_dtos")
    private GithubCommitsDTO[] githubCommitsDTOS;
    @JsonProperty("github_comments_dtos")
    private GithubCommentsDTO[] githubCommentsDTOS;

}
