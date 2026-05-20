package com.esprit.campconnect.Formation.dto.stats;

import java.time.LocalDate;

public class FormationViewsEvolutionPointDto {

    private LocalDate date;
    private long viewsCount;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public long getViewsCount() {
        return viewsCount;
    }

    public void setViewsCount(long viewsCount) {
        this.viewsCount = viewsCount;
    }
}
