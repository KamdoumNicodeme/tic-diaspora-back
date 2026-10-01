package com.ticdiaspora.application.port.out;

import com.ticdiaspora.domain.model.*;
import com.ticdiaspora.domain.model.enums.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.*;
import java.util.*;

public interface MeetingRepositoryPort {
    Page<Meeting> findAll(Pageable pageable);

    List<Meeting> findAll();

    Optional<Meeting> findById(UUID id);

    Meeting save(Meeting meeting);

    Optional<Meeting> findFirstByMeetingDateGreaterThanEqualAndStatusOrderByMeetingDateAsc(LocalDate date, MeetingStatus status);

    boolean existsByMeetingDate(LocalDate meetingDate);

    List<Meeting> findAllByMeetingDateBetweenOrderByMeetingDateAsc(LocalDate from, LocalDate to);
}
