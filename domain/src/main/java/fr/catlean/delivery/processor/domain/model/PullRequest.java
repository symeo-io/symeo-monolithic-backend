package fr.catlean.delivery.processor.domain.model;

import lombok.Builder;
import lombok.Value;

import java.util.Date;

@Value
@Builder
public class PullRequest {
    private static final String ALL = "pull_requests";
    int id;
    int commitNumber;
    int deletedLineNumber;
    int addedLineNumber;
    Date creationDate;
    Date lastUpdateDate;
    Date mergeDate;
    Boolean isMerged;
    Boolean isDraft;
    String state;
    int number;
    String vcsUrl;
    String title;
    String authorLogin;

    public static String getNameFromRepository(String repositoryName) {
        return ALL + "_" + repositoryName;
    }
}
