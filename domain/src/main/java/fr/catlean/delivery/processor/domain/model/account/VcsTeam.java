package fr.catlean.delivery.processor.domain.model.account;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class VcsTeam {
    @NonNull
    String name;
    @NonNull
    List<String> vcsRepositoryNames;
    @NonNull
    Integer pullRequestLineNumberLimit;
    @NonNull
    Integer pullRequestDayNumberLimit;
}
