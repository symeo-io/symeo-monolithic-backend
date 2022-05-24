package fr.catlean.delivery.processor.domain.port.out;

public interface VersionControlSystemAdapter {
  byte[] getRawRepositories(String organisation);

  String getName();
}
