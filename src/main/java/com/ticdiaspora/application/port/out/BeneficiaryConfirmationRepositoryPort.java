package com.ticdiaspora.application.port.out;

import com.ticdiaspora.domain.model.*;
import com.ticdiaspora.domain.model.enums.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.*;
import java.util.*;

public interface BeneficiaryConfirmationRepositoryPort {
    Optional<BeneficiaryConfirmation> findByCycleId(UUID cycleId);

    BeneficiaryConfirmation save(BeneficiaryConfirmation beneficiaryConfirmation);
}
