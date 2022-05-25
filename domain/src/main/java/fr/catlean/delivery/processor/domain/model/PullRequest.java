package fr.catlean.delivery.processor.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PullRequest {
    private static final String ALL = "pull_requests";
    int id;

    public static String getNameFromRepository(String repositoryName) {
        return ALL + "_" + repositoryName;
    }
}
