package fr.catlean.delivery.processor.domain.port.out;

public interface RawStorageAdapter {
  void save(String organisation, String date, String vcsAdapterName, byte[] bytes);
}
