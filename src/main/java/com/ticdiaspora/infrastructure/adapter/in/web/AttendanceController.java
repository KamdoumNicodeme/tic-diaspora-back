package com.ticdiaspora.infrastructure.adapter.in.web;
import com.ticdiaspora.infrastructure.adapter.in.web.mapper.AttendanceWebMapper;

import com.ticdiaspora.application.usecase.*;
import com.ticdiaspora.application.port.out.*;
import com.ticdiaspora.infrastructure.adapter.in.web.dto.*;

import com.ticdiaspora.domain.model.AttendanceRecord;
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
    private final AttendanceWebMapper attendanceMapper;

    public AttendanceController(AttendanceService attendanceService, AttendanceWebMapper attendanceMapper) {
        this.attendanceService = attendanceService;
        this.attendanceMapper = attendanceMapper;
    }

    @GetMapping("/meetings/{meetingId}")
    @PreAuthorize("hasRole('PRESIDENT')")
    List<AttendanceDtos.AttendanceResponse> byMeeting(@PathVariable UUID meetingId) {
        return attendanceService.byMeeting(meetingId).stream().map(attendanceMapper::toResponse).toList();
    }

    @PostMapping("/check-in")
    @PreAuthorize("hasRole('PRESIDENT')")
    AttendanceDtos.AttendanceResponse checkIn(@Valid @RequestBody AttendanceDtos.CheckInRequest request) {
        return attendanceMapper.toResponse(attendanceService.checkIn(request.meetingId(), request.memberId(), request.arrivalAt()));
    }

    @PostMapping("/absence")
    @PreAuthorize("hasRole('PRESIDENT')")
    AttendanceDtos.AttendanceResponse recordAbsence(@Valid @RequestBody AttendanceDtos.AbsenceRecordRequest request) {
        return attendanceMapper.toResponse(attendanceService.recordAbsence(request.meetingId(), request.memberId(), request.authorized(), request.absenceRequestId()));
    }

    private AttendanceDtos.AttendanceResponse toResponse(AttendanceRecord record) {
        return new AttendanceDtos.AttendanceResponse(
                record.getId(), record.getMeetingId(), record.getMemberId(), record.getStatus(),
                record.getExpectedStartAt(), record.getArrivalAt(), record.getDelayMinutes(),
                record.getPenaltyId(), record.getAbsenceRequestId(), record.getRecordedBy(), record.getCreatedAt()
        );
    }
}
