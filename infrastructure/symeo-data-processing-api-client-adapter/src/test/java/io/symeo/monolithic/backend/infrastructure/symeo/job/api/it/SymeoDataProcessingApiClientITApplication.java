package io.symeo.monolithic.backend.infrastructure.symeo.job.api.it;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(SymeoDataProcessingApiClientITConfiguration.class)
public class SymeoDataProcessingApiClientITApplication {

    public static void main(String[] args) {
        SpringApplication.run(SymeoDataProcessingApiClientITApplication.class, args);
    }


}
