package fr.catlean.monolithic.backend.infrastructure.aws.s3.adapter;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class AmazonS3RawStorageAdapterTest {


    private final Faker faker = new Faker();

    @Test
    void should_save_bytes_to_s3_bucket_storage() throws CatleanException {
        // Given
        final AmazonS3 amazonS3 = mock(AmazonS3.class);
        final AmazonS3Properties amazonS3Properties = buildAmazonS3PropertiesStub();
        final AmazonS3RawStorageAdapter amazonS3RawStorageAdapter =
                new AmazonS3RawStorageAdapter(amazonS3Properties, amazonS3);
        final byte[] bytes = faker.shakespeare().romeoAndJulietQuote().getBytes();
        final String organization = faker.animal().name();
        final String adapterName = faker.app().name();
        final String contentName = faker.pokemon().name();
        final PutObjectResult putObjectResultMock = mock(PutObjectResult.class);

        // When
        when(amazonS3.putObject(anyString(), anyString(), any(), any())).thenReturn(putObjectResultMock);
        when(putObjectResultMock.getContentMd5()).thenReturn(new String(Base64.getEncoder().encode(DigestUtils.md5(bytes))));
        when(amazonS3.doesBucketExistV2(amazonS3Properties.getRawBucketName())).thenReturn(true);
        amazonS3RawStorageAdapter.save(organization, adapterName, contentName, bytes);

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
        final String organization = faker.animal().name();
        final String adapterName = faker.app().name();
        final String contentName = faker.pokemon().name();

        // When
        when(amazonS3.doesBucketExistV2(amazonS3Properties.getRawBucketName())).thenReturn(true);
        when(amazonS3.putObject(anyString(), anyString(), any(), any())).thenThrow(new SdkClientException(faker.name().firstName()));
        CatleanException exception = null;
        try {
            amazonS3RawStorageAdapter.save(organization, adapterName, contentName, bytes);
        } catch (CatleanException e) {
            exception = e;
        }

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo("T.AWS_API_EXCEPTION");
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
        final String organization = faker.animal().name();
        final String adapterName = faker.app().name();
        final String contentName = faker.pokemon().name();
        final PutObjectResult putObjectResultMock = mock(PutObjectResult.class);

        // When
        when(amazonS3.putObject(anyString(), anyString(), any(), any())).thenReturn(putObjectResultMock);
        when(putObjectResultMock.getContentMd5()).thenReturn(faker.ancient().hero());
        when(amazonS3.doesBucketExistV2(amazonS3Properties.getRawBucketName())).thenReturn(true);
        CatleanException exception = null;
        try {
            amazonS3RawStorageAdapter.save(organization, adapterName, contentName, bytes);
        } catch (CatleanException e) {
            exception = e;
        }

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo("T.PARTIAL_S3_UPLOAD");
        assertThat(exception.getMessage()).isEqualTo("Failed to upload report " + organization + "/" + adapterName +
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
        final String organization = faker.animal().name();
        final String adapterName = faker.app().name();
        final String contentName = faker.pokemon().name();

        // When
        when(amazonS3.doesBucketExistV2(amazonS3Properties.getRawBucketName())).thenReturn(false);
        CatleanException exception = null;
        try {
            amazonS3RawStorageAdapter.save(organization, adapterName, contentName, bytes);
        } catch (CatleanException e) {
            exception = e;
        }

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo("F.INVALID_BUCKET_NAME");
        assertThat(exception.getMessage()).isEqualTo("Failed to upload report " + organization + "/" + adapterName +
                "/" + contentName + ".json" + " to S3 bucket " + amazonS3Properties.getRawBucketName() + " :" +
                " the bucket does not exist.");
    }


    private AmazonS3Properties buildAmazonS3PropertiesStub() {
        final AmazonS3Properties amazonS3Properties = new AmazonS3Properties();
        amazonS3Properties.setAccessKey(faker.pokemon().name());
        amazonS3Properties.setRegion(faker.university().name());
        amazonS3Properties.setRawBucketName(faker.name().name());
        amazonS3Properties.setSecretKey(faker.harryPotter().book());
        return amazonS3Properties;
    }
}

