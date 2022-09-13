package io.symeo.monolithic.backend.domain.job;

import lombok.*;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    public static final String TO_DO = "TO_DO";
    @NonNull
    Object input;
    @NonNull
    @Builder.Default
    String status = TO_DO;
}
