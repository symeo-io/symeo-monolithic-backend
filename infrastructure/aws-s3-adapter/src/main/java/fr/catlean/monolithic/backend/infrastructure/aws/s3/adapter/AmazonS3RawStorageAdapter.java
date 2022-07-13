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
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

@AllArgsConstructor
@Slf4j
public class AmazonS3RawStorageAdapter implements RawStorageAdapter {

    private final AmazonS3Properties amazonS3Properties;
    private final AmazonS3 amazonS3;

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
                    ".AWS_S3_SERIALIZATION_EXCEPTION").build();
        }
    }

    @Override
    public boolean exists(String organization, String adapterName, String contentName) throws CatleanException {
        try {
            return amazonS3.doesObjectExist(amazonS3Properties.getRawBucketName(), getBucketKey(organization,
                    adapterName, contentName));
        } catch (SdkClientException sdkClientException) {
            LOGGER.error("Error while checking if bucket {} {} {} exists", organization, adapterName,
                    contentName, sdkClientException);
            throw CatleanException.builder()
                    .message("A technical error happened with AWS API")
                    .code("T.AWS_API_EXCEPTION")
                    .build();
        }
    }


    private void uploadByteArrayToS3Bucket(final byte[] byteArray, final String bucketName, final String bucketKey) throws CatleanException {
        final String md5 = new String(Base64.getEncoder().encode(DigestUtils.md5(byteArray)));
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
        try {
            final String md5FromUploadedFile = putObjectToS3andGetContentFileUploadedMd5(bucketName, bucketKey,
                    byteArrayInputStream);
            if (!md5.equals(md5FromUploadedFile)) {
                LOGGER.error("Bucket {} {} md5 content is not equaled to file md5 content", bucketName, bucketKey);
                throw CatleanException.builder()
                        .message("Failed to upload report " + bucketKey + " to S3 bucket " + bucketName + " : md5s " +
                                "are not equaled, it should be a partial upload")
                        .code("T.PARTIAL_S3_UPLOAD")
                        .build();
            }
        } catch (SdkClientException sdkClientException) {
            LOGGER.error("A technical exception happened with AWS SDK Client", sdkClientException);
            throw CatleanException.builder()
                    .message("A technical error happened with AWS API")
                    .code("T.AWS_API_EXCEPTION")
                    .build();
        }
    }

    private String putObjectToS3andGetContentFileUploadedMd5(String bucketStorage, String bucketKeyId,
                                                             ByteArrayInputStream byteArrayInputStream) throws CatleanException, SdkClientException {
        if (amazonS3.doesBucketExistV2(bucketStorage)) {
            final PutObjectResult putObjectResult = amazonS3.putObject(bucketStorage, bucketKeyId,
                    byteArrayInputStream, new ObjectMetadata());
            return putObjectResult.getContentMd5();
        } else {
            LOGGER.error("Failed to upload report {} to S3 bucket {}", bucketKeyId, bucketStorage);
            throw CatleanException.builder()
                    .message("Failed to upload report " + bucketKeyId + " to S3 bucket " + bucketStorage + " :" +
                            " the bucket does not exist.")
                    .code("F.INVALID_BUCKET_NAME")
                    .build();
        }
    }
}
