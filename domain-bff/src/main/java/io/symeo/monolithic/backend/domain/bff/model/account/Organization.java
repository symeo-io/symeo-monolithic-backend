package io.symeo.monolithic.backend.domain.bff.model.account;

import lombok.*;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Organization {
    UUID id;
    String name;
    @Builder.Default
    List<Team> teams = new ArrayList<>();
    @Builder.Default
    TimeZone timeZone = TimeZone.getTimeZone(ZoneId.systemDefault());
    VcsOrganization vcsOrganization;

    @Builder
    @Value
    public static class VcsOrganization {
        String id;
        String name;
        String vcsId;
        String externalId;
    }
}
