package fr.catlean.delivery.processor.infrastructure.json.local.storage;

import fr.catlean.delivery.processor.domain.port.out.RawStorageAdapter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonLocalStorageAdapter implements RawStorageAdapter {

  private final String rootDirectory;

  public JsonLocalStorageAdapter(String rootDirectory) {
    this.rootDirectory = rootDirectory;
  }

  @Override
  public void save(
      String organisation, String date, String adapterName, String contentName, byte[] bytes) {
    final Path jsonPath =
        Path.of(
            rootDirectory
                + "/"
                + organisation
                + "/"
                + date
                + "/"
                + adapterName
                + "/"
                + contentName
                + ".json");
    if (!Files.exists(jsonPath)) {
      try {
        Files.createDirectories(jsonPath.getParent());
        Files.write(jsonPath, bytes);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
