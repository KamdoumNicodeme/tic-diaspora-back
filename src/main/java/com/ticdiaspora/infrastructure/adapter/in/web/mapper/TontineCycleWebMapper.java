package com.ticdiaspora.infrastructure.adapter.in.web.mapper;

import com.ticdiaspora.application.port.in.TontineCycleCommand;
import com.ticdiaspora.domain.model.TontineCycle;
import com.ticdiaspora.infrastructure.adapter.in.web.dto.TontineDtos;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TontineCycleWebMapper {
    TontineCycleCommand toCommand(TontineDtos.TontineCycleRequest request);

    @Mapping(target = "beneficiaryFullName", ignore = true)
    @Mapping(target = "beneficiaryOrangeMoneyNumber", ignore = true)
    @Mapping(target = "beneficiaryOrangeMoneyAccountName", ignore = true)
    @Mapping(target = "beneficiaryMtnMoneyNumber", ignore = true)
    @Mapping(target = "beneficiaryMtnMoneyAccountName", ignore = true)
    @Mapping(target = "beneficiaryExpectedAmount", ignore = true)
    @Mapping(target = "secondaryBeneficiaryFullName", ignore = true)
    @Mapping(target = "secondaryBeneficiaryOrangeMoneyNumber", ignore = true)
    @Mapping(target = "secondaryBeneficiaryOrangeMoneyAccountName", ignore = true)
    @Mapping(target = "secondaryBeneficiaryMtnMoneyNumber", ignore = true)
    @Mapping(target = "secondaryBeneficiaryMtnMoneyAccountName", ignore = true)
    @Mapping(target = "secondaryBeneficiaryExpectedAmount", ignore = true)
    TontineDtos.TontineCycleResponse toResponse(TontineCycle cycle);
}
