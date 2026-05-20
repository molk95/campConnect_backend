package com.esprit.campconnect.Formation.dto.stats;

public class FormationStatsTopItemDto {

    private Long formationId;
    private String title;
    private long count;

    public Long getFormationId() {
        return formationId;
    }

    public void setFormationId(Long formationId) {
        this.formationId = formationId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
