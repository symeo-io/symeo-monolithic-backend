package io.symeo.monolithic.backend.domain.bff.model.account;

import io.symeo.monolithic.backend.domain.bff.model.vcs.RepositoryView;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class Team {
    UUID id;
    String name;
    UUID organizationId;
    List<RepositoryView> repositories;
}
