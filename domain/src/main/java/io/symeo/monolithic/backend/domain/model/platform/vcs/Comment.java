package io.symeo.monolithic.backend.domain.model.platform.vcs;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class Comment {
    private static final String ALL = "comments";
    String id;
    Date creationDate;

    public static String getNameFromPullRequest(PullRequest pullRequest) {
        return ALL + "_for_pr_number_" + pullRequest.getNumber();
    }
}
