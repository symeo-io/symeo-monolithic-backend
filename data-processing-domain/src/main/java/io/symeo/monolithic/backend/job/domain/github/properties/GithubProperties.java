package io.symeo.monolithic.backend.job.domain.github.properties;

import lombok.Data;

@Data
public class GithubProperties {
    private String api;
    private String urlHost;
    private int size;
    private String privateKeyCertificatePath;
    private String githubAppId;
}
