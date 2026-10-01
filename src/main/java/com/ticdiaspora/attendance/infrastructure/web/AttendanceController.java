package com.ticdiaspora.attendance.infrastructure.web;

import com.ticdiaspora.attendance.infrastructure.persistence.AttendanceRecordEntity;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping("/meetings/{meetingId}")
    @PreAuthorize("hasRole('PRESIDENT')")
    List<AttendanceDtos.AttendanceResponse> byMeeting(@PathVariable UUID meetingId) {
        return attendanceService.byMeeting(meetingId).stream().map(this::toResponse).toList();
    }

    @PostMapping("/check-in")
    @PreAuthorize("hasRole('PRESIDENT')")
    AttendanceDtos.AttendanceResponse checkIn(@Valid @RequestBody AttendanceDtos.CheckInRequest request) {
        return toResponse(attendanceService.checkIn(request.meetingId(), request.memberId(), request.arrivalAt()));
    }

    @PostMapping("/absence")
    @PreAuthorize("hasRole('PRESIDENT')")
    AttendanceDtos.AttendanceResponse recordAbsence(@Valid @RequestBody AttendanceDtos.AbsenceRecordRequest request) {
        return toResponse(attendanceService.recordAbsence(request.meetingId(), request.memberId(), request.authorized(), request.absenceRequestId()));
    }

    private AttendanceDtos.AttendanceResponse toResponse(AttendanceRecordEntity record) {
        return new AttendanceDtos.AttendanceResponse(
                record.getId(), record.getMeetingId(), record.getMemberId(), record.getStatus(),
                record.getExpectedStartAt(), record.getArrivalAt(), record.getDelayMinutes(),
                record.getPenaltyId(), record.getAbsenceRequestId(), record.getRecordedBy(), record.getCreatedAt()
        );
    }
}
