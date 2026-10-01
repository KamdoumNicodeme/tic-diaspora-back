package com.ticdiaspora.infrastructure.notification;

import com.ticdiaspora.application.usecase.MeetingReminderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class MeetingReminderScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MeetingReminderScheduler.class);

    private final MeetingReminderService meetingReminderService;

    public MeetingReminderScheduler(MeetingReminderService meetingReminderService) {
        this.meetingReminderService = meetingReminderService;
    }

    @Scheduled(cron = "${app.reminders.meeting-48h-cron:0 */30 * * * *}")
    public void sendMeetingReminders() {
        int sent = meetingReminderService.send48HourReminders(Instant.now());
        if (sent > 0) {
            LOGGER.info("Sent {} meeting reminder email(s)", sent);
        }
    }
}
