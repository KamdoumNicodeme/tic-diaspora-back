package com.ticdiaspora.application.port.in;

public record CompleteMeetingCommand(
        String notes,
        String decisionsSummary,
        String projectsSummary
) {
}
