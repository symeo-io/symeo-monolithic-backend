package io.symeo.monolithic.backend.domain.port.out;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.TeamStandard;

public interface TeamStandardStorage {
    TeamStandard getByCode(String standardCode) throws SymeoException;
}
