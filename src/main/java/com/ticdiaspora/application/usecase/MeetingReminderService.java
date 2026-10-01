package com.ticdiaspora.application.usecase;

import com.ticdiaspora.application.annotation.UseCase;
import com.ticdiaspora.application.port.out.MeetingRepositoryPort;
import com.ticdiaspora.application.port.out.MemberRepositoryPort;
import com.ticdiaspora.domain.model.Meeting;
import com.ticdiaspora.domain.model.Member;
import com.ticdiaspora.domain.model.enums.MeetingStatus;
import com.ticdiaspora.domain.model.enums.MemberStatus;
import com.ticdiaspora.domain.model.enums.NotificationChannel;
import com.ticdiaspora.domain.model.enums.NotificationType;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

@UseCase
public class MeetingReminderService {

    private static final Duration REMINDER_OFFSET = Duration.ofHours(48);
    private static final Duration REMINDER_WINDOW = Duration.ofMinutes(30);

    private final MeetingRepositoryPort meetings;
    private final MemberRepositoryPort members;
    private final NotificationService notificationService;

    public MeetingReminderService(
            MeetingRepositoryPort meetings,
            MemberRepositoryPort members,
            NotificationService notificationService
    ) {
        this.meetings = meetings;
        this.members = members;
        this.notificationService = notificationService;
    }

    @Transactional
    public int send48HourReminders(Instant now) {
        Instant windowStart = now.plus(REMINDER_OFFSET).minus(REMINDER_WINDOW);
        Instant windowEnd = now.plus(REMINDER_OFFSET).plus(REMINDER_WINDOW);
        ZoneId zone = ZoneId.systemDefault();
        List<Meeting> dueMeetings = meetings.findAllByMeetingDateBetweenOrderByMeetingDateAsc(
                        windowStart.atZone(zone).toLocalDate(),
                        windowEnd.atZone(zone).toLocalDate()
                ).stream()
                .filter(meeting -> meeting.getStatus() == MeetingStatus.PLANNED)
                .filter(meeting -> {
                    Instant meetingStart = meeting.getMeetingDate()
                            .atTime(meeting.getPlannedStartTime())
                            .atZone(zone)
                            .toInstant();
                    return !meetingStart.isBefore(windowStart) && meetingStart.isBefore(windowEnd);
                })
                .toList();

        List<Member> activeMembers = members.findAllByStatus(MemberStatus.ACTIVE);
        int sent = 0;
        for (Meeting meeting : dueMeetings) {
            for (Member member : activeMembers) {
                if (sendReminderIfNeeded(member, meeting)) {
                    sent++;
                }
            }
        }
        return sent;
    }

    private boolean sendReminderIfNeeded(Member member, Meeting meeting) {
        String payload = "{\"meetingId\":\"" + meeting.getId() + "\",\"reminder\":\"48h\"}";
        if (notificationService.alreadyNotified(member.getId(), NotificationType.MEETING_REMINDER, NotificationChannel.EMAIL, payload)) {
            return false;
        }
        String title = "Rappel réunion TIC Diaspora";
        String message = buildReminderMessage(member, meeting);
        notificationService.notifyInApp(member.getId(), NotificationType.MEETING_REMINDER, title, message, payload);
        notificationService.notifyEmail(member.getId(), member.getEmail(), NotificationType.MEETING_REMINDER, title, message, payload);
        return true;
    }

    private String buildReminderMessage(Member member, Meeting meeting) {
        String onlineLink = meeting.getOnlineLink() == null || meeting.getOnlineLink().isBlank()
                ? "Lien à confirmer"
                : meeting.getOnlineLink();
        return """
                Bonjour %s,

                Rappel : la prochaine réunion TIC Diaspora aura lieu dans environ 48 heures.

                Réunion : %s
                Date : %s
                Heure : %s - %s
                Lien : %s

                Si vous ne pouvez pas participer, pensez à déclarer votre absence dans la plateforme.

                TIC Diaspora
                """.formatted(
                member.getFirstName(),
                meeting.getTitle(),
                meeting.getMeetingDate(),
                meeting.getPlannedStartTime(),
                meeting.getPlannedEndTime(),
                onlineLink
        );
    }
}
