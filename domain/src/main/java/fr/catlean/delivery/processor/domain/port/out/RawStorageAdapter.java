package fr.catlean.delivery.processor.domain.port.out;

public interface RawStorageAdapter {
    void save(String organization, String adapterName, String contentName, byte[] bytes);

    byte[] read(String organization, String adapterName, String contentName);

    boolean exists(String organization, String adapterName, String contentName);
}
