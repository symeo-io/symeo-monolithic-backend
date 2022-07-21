package fr.catlean.monolithic.backend.domain.job;

import fr.catlean.monolithic.backend.domain.port.in.DataProcessingJobAdapter;

import java.util.function.BiConsumer;

public interface DataProcessingJobExecutor extends BiConsumer<DataProcessingJobAdapter, String> {

}
