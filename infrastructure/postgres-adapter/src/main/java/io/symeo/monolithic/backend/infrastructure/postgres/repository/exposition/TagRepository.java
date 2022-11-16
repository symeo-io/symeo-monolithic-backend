package io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition;

import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.TagEntity;
import org.springframework.beans.PropertyValues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TagRepository extends JpaRepository<TagEntity, String> {

    @Query(nativeQuery = true, value = "select * from exposition_storage.tag t" +
            "    where t.repository_id in (select ttr.repository_id" +
            "                                 from exposition_storage.team_to_repository ttr" +
            "                                 where ttr.team_id = :teamId)")
    List<TagEntity> findAllForTeamId(UUID teamId);

    @Query(nativeQuery = true, value = "select * from exposition_storage.tag t " +
    " where t.repository_id = :repositoryId")
    List<TagEntity> findAllForRepositoryId(@Param("repositoryId") String repositoryId);
}
