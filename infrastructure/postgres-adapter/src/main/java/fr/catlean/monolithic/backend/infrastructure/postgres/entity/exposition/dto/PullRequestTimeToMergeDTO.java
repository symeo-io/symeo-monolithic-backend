package fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.dto;

import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "pull_request", schema = "exposition_storage")
@Entity
public class PullRequestTimeToMergeDTO {

    @Id
    @Column(name = "id", nullable = false)
    @NotNull
    String id;
    @Column(name = "days_opened")
    int daysOpened;
    @Column(name = "creation_date")
    Date creationDate;
    @Column(name = "merge_date")
    Date mergeDate;
    @Column(name = "close_date")
    Date closeDate;
    @Column(name = "state")
    String state;
}
