package fr.catlean.monolithic.backend.infrastructure.github.adapter.unit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AbstractGithubAdapterTest {

    protected final Faker faker = Faker.instance();
    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected  <T> T getStubsFromClassT(final String testResourcesDir, final String fileName, final Class<T> tClass) throws IOException {
        final String dto1 = Files.readString(Paths.get("target/test-classes/"+testResourcesDir+"/" + fileName));
        return objectMapper.readValue(dto1,tClass);
    }

    protected  <T> byte[] dtoStubsToBytes(T[]... dtos) throws JsonProcessingException {
        final List<T> dtoList = new ArrayList<>();
        for (T[] dto : dtos) {
            dtoList.addAll(Arrays.stream(dto).toList());
        }
        return objectMapper.writeValueAsBytes(dtoList.toArray());
    }
}
