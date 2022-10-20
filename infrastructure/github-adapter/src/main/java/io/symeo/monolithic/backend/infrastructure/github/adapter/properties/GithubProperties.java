package io.symeo.monolithic.backend.infrastructure.github.adapter.properties;

import lombok.Data;

@Data
public class GithubProperties {
    private String api;
    private String urlHost;
    private int size;
    private String privateKeyCertificatePath;
    private String githubAppId;
}
