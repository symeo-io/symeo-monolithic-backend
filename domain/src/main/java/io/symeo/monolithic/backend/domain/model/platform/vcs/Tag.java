package io.symeo.monolithic.backend.domain.model.platform.vcs;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class Tag {
    private static final String ALL = "commits";
    String name;
    String commitSha;
    Repository repository;
    String vcsUrl;

    public static String getNameFromRepository(final Repository repository) {
        return ALL + "_for_repository_" + repository.getId();
    }
}
