package io.symeo.monolithic.backend.domain.model.account;

import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
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
    List<Repository> repositories;
}
