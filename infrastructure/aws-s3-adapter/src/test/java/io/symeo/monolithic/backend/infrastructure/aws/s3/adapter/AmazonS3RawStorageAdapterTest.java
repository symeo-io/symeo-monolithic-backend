package io.symeo.monolithic.backend.infrastructure.aws.s3.adapter;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.client.methods.HttpGet;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class AmazonS3RawStorageAdapterTest {


    private final Faker faker = new Faker();

    @Test
    void should_save_bytes_to_s3_bucket_storage() throws SymeoException {
        // Given
        final AmazonS3 amazonS3 = mock(AmazonS3.class);
        final AmazonS3Properties amazonS3Properties = buildAmazonS3PropertiesStub();
        final AmazonS3RawStorageAdapter amazonS3RawStorageAdapter =
                new AmazonS3RawStorageAdapter(amazonS3Properties, amazonS3);
        final byte[] bytes = faker.shakespeare().romeoAndJulietQuote().getBytes();
        final UUID organizationId = UUID.randomUUID();
        final String adapterName = faker.app().name();
        final String contentName = faker.pokemon().name();
        final PutObjectResult putObjectResultMock = mock(PutObjectResult.class);

        // When
        when(amazonS3.putObject(anyString(), anyString(), any(), any())).thenReturn(putObjectResultMock);
        when(putObjectResultMock.getContentMd5()).thenReturn(new String(Base64.getEncoder().encode(DigestUtils.md5(bytes))));
        when(amazonS3.doesBucketExistV2(amazonS3Properties.getRawBucketName())).thenReturn(true);
        amazonS3RawStorageAdapter.save(organizationId, adapterName, contentName, bytes);

        // Then
        verify(amazonS3, times(1)).putObject(any(), any(), any(), any());
    }


    @Test
    void should_raise_an_exception_when_client_raise_a_runtime_while_saving() {
        // Given
        final AmazonS3 amazonS3 = mock(AmazonS3.class);
        final AmazonS3Properties amazonS3Properties = buildAmazonS3PropertiesStub();
        final AmazonS3RawStorageAdapter amazonS3RawStorageAdapter =
                new AmazonS3RawStorageAdapter(amazonS3Properties, amazonS3);
        final byte[] bytes = faker.shakespeare().romeoAndJulietQuote().getBytes();
        final UUID organizationId = UUID.randomUUID();
        final String adapterName = faker.app().name();
        final String contentName = faker.pokemon().name();

        // When
        when(amazonS3.doesBucketExistV2(amazonS3Properties.getRawBucketName())).thenReturn(true);
        when(amazonS3.putObject(anyString(), anyString(), any(), any())).thenThrow(new SdkClientException(faker.name().firstName()));
        SymeoException exception = null;
        try {
            amazonS3RawStorageAdapter.save(organizationId, adapterName, contentName, bytes);
        } catch (SymeoException e) {
            exception = e;
        }

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(AWS_API_EXCEPTION);
        assertThat(exception.getMessage()).isEqualTo("A technical error happened with AWS API");
    }

    @Test
    void should_raise_an_exception_for_not_equaled_md5_while_saving() {
        // Given
        final AmazonS3 amazonS3 = mock(AmazonS3.class);
        final AmazonS3Properties amazonS3Properties = buildAmazonS3PropertiesStub();
        final AmazonS3RawStorageAdapter amazonS3RawStorageAdapter =
                new AmazonS3RawStorageAdapter(amazonS3Properties, amazonS3);
        final byte[] bytes = faker.shakespeare().romeoAndJulietQuote().getBytes();
        final UUID organizationId = UUID.randomUUID();
        final String adapterName = faker.app().name();
        final String contentName = faker.pokemon().name();
        final PutObjectResult putObjectResultMock = mock(PutObjectResult.class);

        // When
        when(amazonS3.putObject(anyString(), anyString(), any(), any())).thenReturn(putObjectResultMock);
        when(putObjectResultMock.getContentMd5()).thenReturn(faker.ancient().hero());
        when(amazonS3.doesBucketExistV2(amazonS3Properties.getRawBucketName())).thenReturn(true);
        SymeoException exception = null;
        try {
            amazonS3RawStorageAdapter.save(organizationId, adapterName, contentName, bytes);
        } catch (SymeoException e) {
            exception = e;
        }

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(AWS_PARTIAL_S3_UPLOAD);
        assertThat(exception.getMessage()).isEqualTo("Failed to upload report " + organizationId + "/" + adapterName +
                "/" + contentName + ".json" + " to S3 bucket " + amazonS3Properties.getRawBucketName() + " : md5s " +
                "are not equaled, it should be a partial upload");
    }


    @Test
    void should_raise_an_exception_for_not_existing_bucket_while_saving() {
        // Given
        final AmazonS3 amazonS3 = mock(AmazonS3.class);
        final AmazonS3Properties amazonS3Properties = buildAmazonS3PropertiesStub();
        final AmazonS3RawStorageAdapter amazonS3RawStorageAdapter =
                new AmazonS3RawStorageAdapter(amazonS3Properties, amazonS3);
        final byte[] bytes = faker.shakespeare().romeoAndJulietQuote().getBytes();
        final UUID organizationId = UUID.randomUUID();
        final String adapterName = faker.app().name();
        final String contentName = faker.pokemon().name();

        // When
        when(amazonS3.doesBucketExistV2(amazonS3Properties.getRawBucketName())).thenReturn(false);
        SymeoException exception = null;
        try {
            amazonS3RawStorageAdapter.save(organizationId, adapterName, contentName, bytes);
        } catch (SymeoException e) {
            exception = e;
        }

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(AWS_INVALID_BUCKET_NAME);
        assertThat(exception.getMessage()).isEqualTo("Failed to upload report " + organizationId + "/" + adapterName +
                "/" + contentName + ".json" + " to S3 bucket " + amazonS3Properties.getRawBucketName() + " :" +
                " the bucket does not exist.");
    }

    @Test
    void should_return_if_the_bucket_content_exists() throws SymeoException {
        // Given
        final AmazonS3 amazonS3 = mock(AmazonS3.class);
        final AmazonS3Properties amazonS3Properties = buildAmazonS3PropertiesStub();
        final AmazonS3RawStorageAdapter amazonS3RawStorageAdapter =
                new AmazonS3RawStorageAdapter(amazonS3Properties, amazonS3);
        final UUID organizationId = UUID.randomUUID();
        final String adapterName = faker.app().name();
        final String contentName = faker.pokemon().name();

        // When
        when(amazonS3.doesObjectExist(amazonS3Properties.getRawBucketName(), organizationId + "/" + adapterName +
                "/" + contentName + ".json")).thenReturn(true);
        final boolean exists = amazonS3RawStorageAdapter.exists(organizationId, adapterName, contentName);


        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void should_raise_exception_when_check_if_the_bucket_exists() {
        // Given
        final AmazonS3 amazonS3 = mock(AmazonS3.class);
        final AmazonS3Properties amazonS3Properties = buildAmazonS3PropertiesStub();
        final AmazonS3RawStorageAdapter amazonS3RawStorageAdapter =
                new AmazonS3RawStorageAdapter(amazonS3Properties, amazonS3);
        final UUID organizationId = UUID.randomUUID();
        final String adapterName = faker.app().name();
        final String contentName = faker.pokemon().name();

        // When
        when(amazonS3.doesObjectExist(amazonS3Properties.getRawBucketName(), organizationId + "/" + adapterName +
                "/" + contentName + ".json")).thenThrow(new SdkClientException(faker.name().firstName()));
        SymeoException exception = null;
        try {
            amazonS3RawStorageAdapter.exists(organizationId, adapterName, contentName);
        } catch (SymeoException e) {
            exception = e;
        }

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(AWS_API_EXCEPTION);
        assertThat(exception.getMessage()).isEqualTo("A technical error happened with AWS API");

    }


    @Test
    void should_read_bucket_content() throws SymeoException {
        // Given
        final AmazonS3 amazonS3 = mock(AmazonS3.class);
        final AmazonS3Properties amazonS3Properties = buildAmazonS3PropertiesStub();
        final AmazonS3RawStorageAdapter amazonS3RawStorageAdapter =
                new AmazonS3RawStorageAdapter(amazonS3Properties, amazonS3);
        final UUID organizationId = UUID.randomUUID();
        final String adapterName = faker.app().name();
        final String contentName = faker.pokemon().name();
        final S3Object s3Object = mock(S3Object.class);
        final byte[] expectedBytes = faker.name().firstName().getBytes();

        // When
        when(amazonS3.getObject(any())).thenReturn(s3Object);
        when(s3Object.getObjectContent()).thenReturn(new S3ObjectInputStream(new ByteArrayInputStream(expectedBytes),
                new HttpGet()));
        final byte[] bytes = amazonS3RawStorageAdapter.read(organizationId, adapterName, contentName);

        // Then
        assertThat(bytes).isEqualTo(expectedBytes);
    }


    @Test
    void should_raise_a_technical_exception_while_reading_bucket_content() {
        // Given
        final AmazonS3 amazonS3 = mock(AmazonS3.class);
        final AmazonS3Properties amazonS3Properties = buildAmazonS3PropertiesStub();
        final AmazonS3RawStorageAdapter amazonS3RawStorageAdapter =
                new AmazonS3RawStorageAdapter(amazonS3Properties, amazonS3);
        final UUID organizationId = UUID.randomUUID();
        final String adapterName = faker.app().name();
        final String contentName = faker.pokemon().name();

        // When
        when(amazonS3.getObject(any())).thenThrow(new SdkClientException(faker.name().firstName()));
        SymeoException exception = null;
        try {
            amazonS3RawStorageAdapter.read(organizationId, adapterName, contentName);
        } catch (SymeoException e) {
            exception = e;
        }

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(AWS_API_EXCEPTION);
        assertThat(exception.getMessage()).isEqualTo("A technical error happened with AWS API");
    }


    private AmazonS3Properties buildAmazonS3PropertiesStub() {
        final AmazonS3Properties amazonS3Properties = new AmazonS3Properties();
        amazonS3Properties.setRawBucketName(faker.name().name());
        return amazonS3Properties;
    }
}

