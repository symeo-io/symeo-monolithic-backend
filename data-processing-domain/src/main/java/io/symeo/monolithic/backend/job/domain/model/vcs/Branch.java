package io.symeo.monolithic.backend.job.domain.model.vcs;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class Branch {
    public static final String ALL = "branches";
    String name;

    public static String getNameFromRepository(Repository repository) {
        return ALL + "_for_pr_number_" + repository.getId();
    }
}
