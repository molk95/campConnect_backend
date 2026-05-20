package com.esprit.campconnect.Formation.repository.projection;

public interface FormationCountProjection {
    Long getFormationId();

    String getTitre();

    Long getTotalCount();
}
