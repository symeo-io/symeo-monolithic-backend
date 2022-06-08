package fr.catlean.delivery.processor.domain.model.account;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class VcsTeam {
    String name;
    List<String> vcsRepositoryNames;
}
