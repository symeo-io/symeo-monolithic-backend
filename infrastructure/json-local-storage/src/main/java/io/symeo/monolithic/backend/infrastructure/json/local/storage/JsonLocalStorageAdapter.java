package io.symeo.monolithic.backend.infrastructure.json.local.storage;

import io.symeo.monolithic.backend.infrastructure.json.local.storage.properties.JsonStorageProperties;
import io.symeo.monolithic.backend.job.domain.port.out.RawStorageAdapter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class JsonLocalStorageAdapter implements RawStorageAdapter {

    private final JsonStorageProperties jsonStorageProperties;

    public JsonLocalStorageAdapter(final JsonStorageProperties jsonStorageProperties) {
        this.jsonStorageProperties = jsonStorageProperties;
    }

    @Override
    public void save(
            UUID organizationId, String adapterName, String contentName, byte[] bytes) {
        final Path jsonPath = buildJsonPath(organizationId.toString(), adapterName, contentName);
        try {
            if (!Files.exists(jsonPath)) {
                Files.createDirectories(jsonPath.getParent());
            }
            Files.write(jsonPath, bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] read(UUID organizationId, String adapterName, String contentName) {
        final Path jsonPath = buildJsonPath(organizationId.toString(), adapterName, contentName);
        try {
            return Files.readAllBytes(jsonPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean exists(UUID organizationId, String adapterName, String contentName) {
        final Path jsonPath = buildJsonPath(organizationId.toString(), adapterName, contentName);
        return Files.exists(jsonPath);
    }

    private Path buildJsonPath(String organization, String adapterName, String contentName) {
        return Path.of(
                jsonStorageProperties.getRootDirectory()
                        + "/"
                        + organization
                        + "/"
                        + adapterName
                        + "/"
                        + contentName
                        + ".json");
    }

}
