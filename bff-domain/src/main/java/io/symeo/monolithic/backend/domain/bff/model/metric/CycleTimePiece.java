package io.symeo.monolithic.backend.domain.bff.model.metric;

import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Builder(toBuilder = true)
@Value
@Slf4j
public class CycleTimePiece {
    String id;
    Date creationDate;
    Date mergeDate;
    String state;
    String vcsUrl;
    String title;
    String author;
    String repository;
    Long cycleTime;
    Long codingTime;
    Long reviewTime;
    Long deployTime;
}
