package com.ticdiaspora.application.port.out;

import com.ticdiaspora.domain.model.*;
import com.ticdiaspora.domain.model.enums.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.*;
import java.util.*;

public interface TontineCycleRepositoryPort {
    Page<TontineCycle> findAll(Pageable pageable);

    List<TontineCycle> findAll();

    Optional<TontineCycle> findById(UUID id);

    TontineCycle save(TontineCycle tontineCycle);

    Optional<TontineCycle> findByMonthAndYear(int month, int year);
}
