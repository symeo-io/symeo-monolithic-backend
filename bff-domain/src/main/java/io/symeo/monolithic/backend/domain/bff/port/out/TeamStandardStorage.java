package io.symeo.monolithic.backend.domain.bff.port.out;

import io.symeo.monolithic.backend.domain.bff.model.account.TeamStandard;
import io.symeo.monolithic.backend.domain.exception.SymeoException;

public interface TeamStandardStorage {
    TeamStandard getByCode(String standardCode) throws SymeoException;
}
