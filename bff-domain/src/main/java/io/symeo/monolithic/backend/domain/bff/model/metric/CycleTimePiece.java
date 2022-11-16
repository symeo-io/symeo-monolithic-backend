package io.symeo.monolithic.backend.domain.bff.model.metric;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Data
@Slf4j
public class CycleTimePiece {

    public static final List<String> AVAILABLE_CYCLE_TIME_PIECE_SORTING_PARAMETERS = List.of(
            "vcs_repository", "title", "author", "cycle_time", "coding_time", "review_time", "time_to_deploy"
    );

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
