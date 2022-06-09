package fr.catlean.delivery.processor.domain.model;

import lombok.Builder;
import lombok.Value;

import java.util.Date;

@Value
@Builder
public class PullRequest {
    private static final String ALL = "pull_requests";
    String id;
    Integer commitNumber;
    Integer deletedLineNumber;
    Integer addedLineNumber;
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
    String repository;

    public static String getNameFromRepository(String repositoryName) {
        return ALL + "_" + repositoryName;
    }
}
