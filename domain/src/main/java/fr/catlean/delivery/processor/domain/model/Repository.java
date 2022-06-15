package fr.catlean.delivery.processor.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Repository {
    public static final String ALL = "repositories";
    String name;
    String organizationName;
}
