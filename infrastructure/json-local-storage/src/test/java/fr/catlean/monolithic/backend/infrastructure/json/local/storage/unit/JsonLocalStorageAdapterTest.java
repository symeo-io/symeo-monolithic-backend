package fr.catlean.monolithic.backend.infrastructure.json.local.storage.unit;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.infrastructure.json.local.storage.JsonLocalStorageAdapter;
import fr.catlean.monolithic.backend.infrastructure.json.local.storage.properties.JsonStorageProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonLocalStorageAdapterTest {

    private final Faker faker = Faker.instance();
    String tmpDir;

    @BeforeEach
    void setUp() throws IOException {
        tmpDir = Files.createTempDirectory("json_local_storage_adapter_test").toFile().getAbsolutePath();
    }

    @Test
    void should_save_bytes_as_json_file() throws IOException {
        // Given
        final JsonStorageProperties jsonStorageProperties = new JsonStorageProperties();
        jsonStorageProperties.setRootDirectory(tmpDir);
        final JsonLocalStorageAdapter jsonLocalStorageAdapter = new JsonLocalStorageAdapter(jsonStorageProperties);
        final UUID organizationId = UUID.randomUUID();
        final String adapterName = faker.animal().name();
        final String contentName = faker.beer().name();
        final String bytesAsString = "{\"test\": 1111}";

        // When
        jsonLocalStorageAdapter.save(organizationId, adapterName, contentName, bytesAsString.getBytes());

        // Then
        final String string =
                Files.readString(Path.of(tmpDir + "/" + organizationId + "/" + adapterName + "/" + contentName + ".json"
                ));
        assertThat(string).isEqualTo(bytesAsString);
    }

    @Test
    void should_override_bytes_as_json_file() throws IOException {
        // Given
        final JsonStorageProperties jsonStorageProperties = new JsonStorageProperties();
        jsonStorageProperties.setRootDirectory(tmpDir);
        final JsonLocalStorageAdapter jsonLocalStorageAdapter = new JsonLocalStorageAdapter(jsonStorageProperties);
        final UUID organizationId = UUID.randomUUID();
        final String adapterName = faker.animal().name();
        final String contentName = faker.beer().name();
        final String bytesAsString = "{\"test\": 1111}";
        final String bytesAsStringOverride = "{\"test\": 2222}";

        // When
        jsonLocalStorageAdapter.save(organizationId, adapterName, contentName, bytesAsString.getBytes());
        jsonLocalStorageAdapter.save(organizationId, adapterName, contentName, bytesAsStringOverride.getBytes());

        // Then
        final String string =
                Files.readString(Path.of(tmpDir + "/" + organizationId + "/" + adapterName + "/" + contentName + ".json"
                ));
        assertThat(string).isEqualTo(bytesAsStringOverride);
    }

    @Test
    void should_read_bytes_from_json_file() throws IOException {
        // Given
        final JsonStorageProperties jsonStorageProperties = new JsonStorageProperties();
        jsonStorageProperties.setRootDirectory(tmpDir);
        final JsonLocalStorageAdapter jsonLocalStorageAdapter = new JsonLocalStorageAdapter(jsonStorageProperties);
        final UUID organizationId = UUID.randomUUID();
        final String adapterName = faker.animal().name();
        final String contentName = faker.beer().name();
        final String bytesAsString = "{\"test\": 1111}";
        final Path jsonPath =
                Path.of(tmpDir + "/" + organizationId + "/" + adapterName + "/" + contentName + ".json");
        Files.createDirectories(jsonPath.getParent());
        Files.write(jsonPath, bytesAsString.getBytes());

        // When
        final byte[] read = jsonLocalStorageAdapter.read(organizationId, adapterName, contentName);

        // Then
        assertThat(read).isEqualTo(bytesAsString.getBytes());
    }

    @Test
    void should_return_if_a_json_file_exists() {
        // Given
        final JsonStorageProperties jsonStorageProperties = new JsonStorageProperties();
        jsonStorageProperties.setRootDirectory(tmpDir);
        final UUID organizationId = UUID.randomUUID();
        final JsonLocalStorageAdapter jsonLocalStorageAdapter = new JsonLocalStorageAdapter(jsonStorageProperties);
        final String organization = faker.animal().name();
        final String adapterName = faker.animal().name();
        final String contentName = faker.beer().name();
        final String bytesAsString = "{\"test\": 1111}";
        jsonLocalStorageAdapter.save(organizationId, adapterName, contentName, bytesAsString.getBytes());

        // When
        final boolean existingFile = jsonLocalStorageAdapter.exists(organizationId, adapterName, contentName);
        final boolean notExistingFile = jsonLocalStorageAdapter.exists(UUID.randomUUID(),
                faker.name().username(), faker.pokemon().name());

        // Then
        assertThat(existingFile).isTrue();
        assertThat(notExistingFile).isFalse();
    }

    @AfterEach
    void tearDown() {
        new File(tmpDir).delete();
    }
}
