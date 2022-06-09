package fr.catlean.delivery.processor.domain.port.out;

public interface RawStorageAdapter {
    void save(String organisation, String adapterName, String contentName, byte[] bytes);

    byte[] read(String organisation, String adapterName, String contentName);

    boolean exists(String organisation, String adapterName, String contentName);
}
