package fr.catlean.delivery.processor.domain.service;

import fr.catlean.delivery.processor.domain.model.Repository;
import fr.catlean.delivery.processor.domain.port.out.RawStorageAdapter;
import fr.catlean.delivery.processor.domain.port.out.VersionControlSystemAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DeliveryQuery {

    private RawStorageAdapter rawStorageAdapter;
    private VersionControlSystemAdapter versionControlSystemAdapter;
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd-MM-yyyy");

    public DeliveryQuery(RawStorageAdapter rawStorageAdapter, VersionControlSystemAdapter versionControlSystemAdapter) {
        this.rawStorageAdapter = rawStorageAdapter;
        this.versionControlSystemAdapter = versionControlSystemAdapter;
    }

    public List<Repository> readRepositoriesForOrganisation(String organisation) {
        final byte[] repositoriesBytes = rawStorageAdapter.read(organisation, SDF.format(new Date()), versionControlSystemAdapter.getName(), "get_repositories");
        return versionControlSystemAdapter.repositoriesBytesToDomain(repositoriesBytes);
    }
}
