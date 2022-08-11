package fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.dto;

import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "pull_request", schema = "exposition_storage")
@Entity
public class PullRequestSizeDTO {

    @Id
    @Column(name = "id", nullable = false)
    @NotNull
    String id;
    @Column(name = "size")
    int size;
    @Column(name = "start_date_range")
    String startDateRange;
    @Column(name = "state")
    String state;
}
