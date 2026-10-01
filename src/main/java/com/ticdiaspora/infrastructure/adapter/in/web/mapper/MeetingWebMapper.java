package com.ticdiaspora.infrastructure.adapter.in.web.mapper;

import com.ticdiaspora.application.port.in.CompleteMeetingCommand;
import com.ticdiaspora.application.port.in.MeetingCommand;
import com.ticdiaspora.domain.model.Meeting;
import com.ticdiaspora.infrastructure.adapter.in.web.dto.MeetingDtos;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MeetingWebMapper {
    MeetingCommand toCommand(MeetingDtos.MeetingRequest request);

    CompleteMeetingCommand toCommand(MeetingDtos.CompleteMeetingRequest request);

    @Mapping(target = "chairpersonFullName", ignore = true)
    @Mapping(target = "beneficiaryId", ignore = true)
    @Mapping(target = "beneficiaryFullName", ignore = true)
    @Mapping(target = "beneficiaryOrangeMoneyNumber", ignore = true)
    @Mapping(target = "beneficiaryOrangeMoneyAccountName", ignore = true)
    @Mapping(target = "beneficiaryMtnMoneyNumber", ignore = true)
    @Mapping(target = "beneficiaryMtnMoneyAccountName", ignore = true)
    @Mapping(target = "beneficiaryExpectedAmount", ignore = true)
    @Mapping(target = "secondaryBeneficiaryId", ignore = true)
    @Mapping(target = "secondaryBeneficiaryFullName", ignore = true)
    @Mapping(target = "secondaryBeneficiaryOrangeMoneyNumber", ignore = true)
    @Mapping(target = "secondaryBeneficiaryOrangeMoneyAccountName", ignore = true)
    @Mapping(target = "secondaryBeneficiaryMtnMoneyNumber", ignore = true)
    @Mapping(target = "secondaryBeneficiaryMtnMoneyAccountName", ignore = true)
    @Mapping(target = "secondaryBeneficiaryExpectedAmount", ignore = true)
    MeetingDtos.MeetingResponse toResponse(Meeting meeting);
}
