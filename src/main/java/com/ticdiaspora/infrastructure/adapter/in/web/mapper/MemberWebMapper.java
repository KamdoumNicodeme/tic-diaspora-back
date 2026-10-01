package com.ticdiaspora.infrastructure.adapter.in.web.mapper;

import com.ticdiaspora.application.port.in.MemberCommand;
import com.ticdiaspora.application.port.in.MemberProfileCommand;
import com.ticdiaspora.domain.model.Member;
import com.ticdiaspora.infrastructure.adapter.in.web.dto.MemberDtos;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MemberWebMapper {
    MemberCommand toCommand(MemberDtos.MemberRequest request);

    MemberProfileCommand toProfileCommand(MemberDtos.ProfileUpdateRequest request);

    MemberDtos.MemberResponse toResponse(Member member);
}
