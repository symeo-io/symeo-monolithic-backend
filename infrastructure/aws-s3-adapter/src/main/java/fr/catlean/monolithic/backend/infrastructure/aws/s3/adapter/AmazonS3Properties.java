package fr.catlean.monolithic.backend.infrastructure.aws.s3.adapter;

import lombok.Data;

@Data
public class AmazonS3Properties {

    private String accessKey;
    private String secretKey;
    private String region;
    private String rawBucketName;
}
