package fr.catlean.monolithic.backend.infrastructure.aws.s3.adapter;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.port.out.RawStorageAdapter;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

public class AmazonS3RawStorageAdapter implements RawStorageAdapter {

    private final AmazonS3Properties amazonS3Properties;
    private final AmazonS3 amazonS3;

    public AmazonS3RawStorageAdapter(AmazonS3Properties amazonS3Properties) {
        this.amazonS3Properties = amazonS3Properties;
        this.amazonS3 = AmazonS3ClientFactory.getAmazonS3Client(amazonS3Properties);
    }

    @Override
    public void save(String organization, String adapterName, String contentName, byte[] bytes) throws CatleanException {
        uploadByteArrayToS3Bucket(bytes, amazonS3Properties.getRawBucketName(), getBucketKey(organization,
                adapterName, contentName));
    }

    private static String getBucketKey(String organization, String adapterName, String contentName) {
        return organization + "/" + adapterName + "/" + contentName + ".json";
    }

    @Override
    public byte[] read(String organization, String adapterName, String contentName) throws CatleanException {
        try {
            final S3Object s3Object =
                    amazonS3.getObject(new GetObjectRequest(amazonS3Properties.getRawBucketName(),
                            getBucketKey(organization, adapterName, contentName)));
            return IOUtils.toByteArray(s3Object.getObjectContent());
        } catch (SdkClientException sdkClientException) {
            throw CatleanException.builder().message("A technical error happened with AWS API").code("T" +
                    ".AWS_API_EXCEPTION").build();
        } catch (IOException e) {
            throw CatleanException.builder().message("Failed to serialize AWS S3 Content").code("T" +
                    ".AWS_S3_SER_EXCEPTION").build();
        }
    }

    @Override
    public boolean exists(String organization, String adapterName, String contentName) throws CatleanException {
        try {
            return amazonS3.doesObjectExist(amazonS3Properties.getRawBucketName(), getBucketKey(organization,
                    adapterName, contentName));
        } catch (SdkClientException sdkClientException) {
            throw CatleanException.builder()
                    .message("A technical error happened with AWS API")
                    .code("T.AWS_API_EXCEPTION")
                    .build();
        }
    }


    public void uploadByteArrayToS3Bucket(final byte[] byteArray, final String bucketName, final String bucketKey) throws CatleanException {
        final String md5 = new String(Base64.getEncoder().encode(DigestUtils.md5(byteArray)));
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
        try {
            final String md5FromUploadedFile = putObjectToS3andGetContentFileUploadedMd5(bucketName, bucketKey,
                    byteArrayInputStream);
            if (!md5.equals(md5FromUploadedFile)) {
                throw CatleanException.builder()
                        .message("Failed to upload report " + bucketKey + " to S3 bucket " + bucketName + " : md5s " +
                                "are not equaled, it should be a partial upload")
                        .code("T.PARTIAL_S3_UPLOAD")
                        .build();
            }
        } catch (SdkClientException sdkClientException) {
            throw CatleanException.builder()
                    .message("A technical error happened with AWS API")
                    .code("T.AWS_API_EXCEPTION")
                    .build();
        }
    }

    private String putObjectToS3andGetContentFileUploadedMd5(String dv360RawBucketStorage, String reportName,
                                                             ByteArrayInputStream byteArrayInputStream) throws CatleanException, SdkClientException {
        if (amazonS3.doesBucketExistV2(dv360RawBucketStorage)) {
            final PutObjectResult putObjectResult = amazonS3.putObject(dv360RawBucketStorage, reportName,
                    byteArrayInputStream, new ObjectMetadata());
            return putObjectResult.getContentMd5();
        } else {
            throw CatleanException.builder()
                    .message("Failed to upload report " + reportName + " to S3 bucket " + dv360RawBucketStorage + " :" +
                            " the bucket does not exist.")
                    .code("F.INVALID_BUCKET_NAME")
                    .build();
        }
    }
}
