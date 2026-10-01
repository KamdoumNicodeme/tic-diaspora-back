package com.ticdiaspora.domain.model;

import com.ticdiaspora.domain.model.enums.ProjectStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    private UUID id;
    private String title;
    private String description;
    private UUID responsibleId;
    @Builder.Default
    private ProjectStatus status = ProjectStatus.PROPOSED;
    private long estimatedBudget;
    private long realBudget;
    private Instant createdAt;
    private LocalDate startDate;
    private LocalDate endDate;
}
