package fr.catlean.delivery.processor.domain.port.out;

import fr.catlean.delivery.processor.domain.model.Repository;

import java.util.List;

public interface VersionControlSystemAdapter {
    byte[] getRawRepositories(String organisation);

    String getName();

    List<Repository> repositoriesBytesToDomain(byte[] repositoriesBytes);
}
