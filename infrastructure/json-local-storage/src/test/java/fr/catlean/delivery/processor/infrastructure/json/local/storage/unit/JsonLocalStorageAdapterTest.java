package fr.catlean.delivery.processor.infrastructure.json.local.storage.unit;

import com.github.javafaker.Faker;
import fr.catlean.delivery.processor.infrastructure.json.local.storage.JsonLocalStorageAdapter;
import fr.catlean.delivery.processor.infrastructure.json.local.storage.properties.JsonStorageProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
        final String organisation = faker.animal().name();
        final String date = faker.pokemon().name();
        final String adapterName = faker.animal().name();
        final String contentName = faker.beer().name();
        final String bytesAsString = "{\"test\": 1111}";

        // When
        jsonLocalStorageAdapter.save(organisation, date, adapterName, contentName, bytesAsString.getBytes());

        // Then
        final String string =
                Files.readString(Path.of(tmpDir + "/" + organisation + "/" + date + "/" + adapterName + "/" + contentName + ".json"));
        assertThat(string).isEqualTo(bytesAsString);
    }

    @Test
    void should_read_bytes_from_json_file() throws IOException {
        // Given
        final JsonStorageProperties jsonStorageProperties = new JsonStorageProperties();
        jsonStorageProperties.setRootDirectory(tmpDir);
        final JsonLocalStorageAdapter jsonLocalStorageAdapter = new JsonLocalStorageAdapter(jsonStorageProperties);
        final String organisation = faker.animal().name();
        final String date = faker.pokemon().name();
        final String adapterName = faker.animal().name();
        final String contentName = faker.beer().name();
        final String bytesAsString = "{\"test\": 1111}";
        final Path jsonPath =
                Path.of(tmpDir + "/" + organisation + "/" + date + "/" + adapterName + "/" + contentName + ".json");
        Files.createDirectories(jsonPath.getParent());
        Files.write(jsonPath, bytesAsString.getBytes());

        // When
        final byte[] read = jsonLocalStorageAdapter.read(organisation, date, adapterName, contentName);

        // Then
        assertThat(read).isEqualTo(bytesAsString.getBytes());
    }

    @AfterEach
    void tearDown() {
        new File(tmpDir).delete();
    }
}
