package io.symeo.monolithic.backend.infrastructure.aws.s3.adapter;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.port.out.RawStorageAdapter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.exception.SymeoException.getSymeoException;
import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.*;

@AllArgsConstructor
@Slf4j
public class AmazonS3RawStorageAdapter implements RawStorageAdapter {

    private final AmazonS3Properties amazonS3Properties;
    private final AmazonS3 amazonS3;

    @Override
    public void save(UUID organizationId, String adapterName, String contentName, byte[] bytes) throws SymeoException {
        uploadByteArrayToS3Bucket(bytes, amazonS3Properties.getRawBucketName(), getBucketKey(organizationId.toString(),
                adapterName, contentName));
    }

    private static String getBucketKey(String organization, String adapterName, String contentName) {
        return organization + "/" + adapterName + "/" + contentName + ".json";
    }

    @Override
    public byte[] read(UUID organizationId, String adapterName, String contentName) throws SymeoException {
        try {
            final S3Object s3Object =
                    amazonS3.getObject(new GetObjectRequest(amazonS3Properties.getRawBucketName(),
                            getBucketKey(organizationId.toString(), adapterName, contentName)));
            return IOUtils.toByteArray(s3Object.getObjectContent());
        } catch (SdkClientException sdkClientException) {
            throw getSymeoException("A technical error happened with AWS API", AWS_API_EXCEPTION, sdkClientException);
        } catch (IOException e) {
            throw getSymeoException("Failed to serialize AWS S3 Content", AWS_S3_SERIALIZATION_EXCEPTION, e);
        }
    }

    @Override
    public boolean exists(UUID organizationId, String adapterName, String contentName) throws SymeoException {
        try {
            return amazonS3.doesObjectExist(amazonS3Properties.getRawBucketName(),
                    getBucketKey(organizationId.toString(),
                            adapterName, contentName));
        } catch (SdkClientException sdkClientException) {
            LOGGER.error("Error while checking if bucket {} {} {} exists", organizationId, adapterName,
                    contentName, sdkClientException);
            throw getSymeoException("A technical error happened with AWS API", AWS_API_EXCEPTION, sdkClientException);
        }
    }


    private void uploadByteArrayToS3Bucket(final byte[] byteArray, final String bucketName, final String bucketKey) throws SymeoException {
        final String md5 = new String(Base64.getEncoder().encode(DigestUtils.md5(byteArray)));
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
        try {
            final String md5FromUploadedFile = putObjectToS3andGetContentFileUploadedMd5(bucketName, bucketKey,
                    byteArrayInputStream);
            if (!md5.equals(md5FromUploadedFile)) {
                LOGGER.error("Bucket {} {} md5 content is not equaled to file md5 content", bucketName, bucketKey);
                throw getSymeoException("Failed to upload report " + bucketKey + " to S3 bucket " + bucketName + " " +
                        ": md5s " +
                        "are not equaled, it should be a partial upload", AWS_PARTIAL_S3_UPLOAD);
            }
        } catch (SdkClientException sdkClientException) {
            LOGGER.error("A technical exception happened with AWS SDK Client", sdkClientException);
            throw getSymeoException("A technical error happened with AWS API", AWS_API_EXCEPTION, sdkClientException);
        }
    }


    private String putObjectToS3andGetContentFileUploadedMd5(String bucketStorage, String bucketKeyId,
                                                             ByteArrayInputStream byteArrayInputStream) throws SymeoException, SdkClientException {
        if (amazonS3.doesBucketExistV2(bucketStorage)) {
            final ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(byteArrayInputStream.available());
            final PutObjectResult putObjectResult = amazonS3.putObject(bucketStorage, bucketKeyId,
                    byteArrayInputStream, metadata);
            return putObjectResult.getContentMd5();
        } else {
            LOGGER.error("Failed to upload report {} to S3 bucket {}", bucketKeyId, bucketStorage);
            throw getSymeoException("Failed to upload report " + bucketKeyId + " to S3 bucket " + bucketStorage +
                    " :" +
                    " the bucket does not exist.", AWS_INVALID_BUCKET_NAME);
        }
    }
}
