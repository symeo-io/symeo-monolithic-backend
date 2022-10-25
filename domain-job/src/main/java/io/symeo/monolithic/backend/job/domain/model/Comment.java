package io.symeo.monolithic.backend.job.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class Comment {
    private static final String ALL = "comments";
    String id;
    Date creationDate;
}
