package io.symeo.monolithic.backend.domain.job;

import lombok.*;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    public static final String TO_DO = "TO_DO";
    public static final String DONE = "DONE";
    @NonNull
    Object input;
    @NonNull
    @Builder.Default
    String status = TO_DO;

    public Task done() {
        return this.toBuilder().status(DONE).build();
    }
}
