package fr.catlean.delivery.processor.infrastructure.json.local.storage;

import fr.catlean.delivery.processor.domain.port.out.RawStorageAdapter;
import fr.catlean.delivery.processor.infrastructure.json.local.storage.properties.JsonStorageProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonLocalStorageAdapter implements RawStorageAdapter {

    private final JsonStorageProperties jsonStorageProperties;

    public JsonLocalStorageAdapter(final JsonStorageProperties jsonStorageProperties) {
        this.jsonStorageProperties = jsonStorageProperties;
    }

    @Override
    public void save(
            String organization, String adapterName, String contentName, byte[] bytes) {
        final Path jsonPath = buildJsonPath(organization, adapterName, contentName);
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
    public byte[] read(String organization, String adapterName, String contentName) {
        final Path jsonPath = buildJsonPath(organization, adapterName, contentName);
        try {
            return Files.readAllBytes(jsonPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean exists(String organization, String adapterName, String contentName) {
        final Path jsonPath = buildJsonPath(organization, adapterName, contentName);
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
