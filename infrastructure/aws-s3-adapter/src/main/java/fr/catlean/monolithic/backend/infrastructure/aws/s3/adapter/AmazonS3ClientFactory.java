package fr.catlean.monolithic.backend.infrastructure.aws.s3.adapter;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

interface AmazonS3ClientFactory {

  static AmazonS3 getAmazonS3Client(AmazonS3Properties amazonS3Properties) {
    return AmazonS3ClientBuilder.standard()
        .withCredentials(
            new AWSStaticCredentialsProvider(
                new BasicAWSCredentials(
                    amazonS3Properties.getAccessKey(), amazonS3Properties.getSecretKey())))
        .withRegion(Regions.fromName(amazonS3Properties.getRegion()))
        .build();
  }
}
