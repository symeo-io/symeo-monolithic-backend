package io.symeo.monolithic.backend.domain.bff.model.metric;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Data
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
    Long timeToDeploy;
}
