package com.esprit.campconnect.Formation.repository.projection;

import java.time.LocalDate;

public interface ViewsEvolutionProjection {
    LocalDate getMetricDate();

    Long getTotalCount();
}
