package com.ticdiaspora.application.port.out;

import com.ticdiaspora.domain.model.*;
import com.ticdiaspora.domain.model.enums.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.*;
import java.util.*;

public interface MemberRepositoryPort {
    Page<Member> findAll(Pageable pageable);

    List<Member> findAll();

    Optional<Member> findById(UUID id);

    Member save(Member member);

    Optional<Member> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<Member> findAllByStatus(MemberStatus status);

    long countByStatus(MemberStatus status);

    long count();
}
