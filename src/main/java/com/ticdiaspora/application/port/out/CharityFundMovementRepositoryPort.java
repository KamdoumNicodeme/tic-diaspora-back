package com.ticdiaspora.application.port.out;

import com.ticdiaspora.domain.model.*;
import com.ticdiaspora.domain.model.enums.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.*;
import java.util.*;

public interface CharityFundMovementRepositoryPort {
    Page<CharityFundMovement> findAll(Pageable pageable);

    CharityFundMovement save(CharityFundMovement charityFundMovement);

    long balance();

    long totalIncome();

    long totalOutcome();

    long totalIncomeBetween(Instant start, Instant end);

    long totalOutcomeBetween(Instant start, Instant end);

    long count();
}
