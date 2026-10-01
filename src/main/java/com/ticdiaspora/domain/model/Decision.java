package com.ticdiaspora.domain.model;

import com.ticdiaspora.domain.model.enums.DecisionPriority;
import com.ticdiaspora.domain.model.enums.DecisionStatus;
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
public class Decision {

    private UUID id;
    private UUID meetingId;
    private UUID projectId;
    private String title;
    private String description;
    private UUID responsibleId;
    private LocalDate dueDate;
    @Builder.Default
    private DecisionStatus status = DecisionStatus.OPEN;
    @Builder.Default
    private DecisionPriority priority = DecisionPriority.MEDIUM;
    private Instant createdAt;
    private Instant updatedAt;
}
