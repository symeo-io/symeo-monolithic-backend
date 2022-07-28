package fr.catlean.monolithic.backend.domain.port.in;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;

import java.util.UUID;

public interface TeamGoalFacadeAdapter {
    void createTeamGoalForTeam(UUID teamId, String standardCode, Integer value) throws CatleanException;
}
