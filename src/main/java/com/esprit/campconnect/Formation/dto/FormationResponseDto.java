package com.esprit.campconnect.Formation.dto;

import com.esprit.campconnect.Formation.entity.FormationLevel;
import com.esprit.campconnect.Formation.entity.FormationStatus;

import java.time.LocalDateTime;

public class FormationResponseDto {

    private Long id;
    private String titre;
    private String description;
    private FormationLevel level;
    private FormationStatus status;
    private Integer duration;
    private LocalDateTime createdAt;
    private Long guideId;
    private String auteurEmail;
    private String auteurNom;
    private Long likeCount;
    private Boolean likedByCurrentUser;

    public FormationResponseDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public FormationLevel getLevel() {
        return level;
    }

    public void setLevel(FormationLevel level) {
        this.level = level;
    }

    public FormationStatus getStatus() {
        return status;
    }

    public void setStatus(FormationStatus status) {
        this.status = status;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getGuideId() {
        return guideId;
    }

    public void setGuideId(Long guideId) {
        this.guideId = guideId;
    }

    public String getAuteurEmail() {
        return auteurEmail;
    }

    public void setAuteurEmail(String auteurEmail) {
        this.auteurEmail = auteurEmail;
    }

    public String getAuteurNom() {
        return auteurNom;
    }

    public void setAuteurNom(String auteurNom) {
        this.auteurNom = auteurNom;
    }

    public Long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }

    public Boolean getLikedByCurrentUser() {
        return likedByCurrentUser;
    }

    public void setLikedByCurrentUser(Boolean likedByCurrentUser) {
        this.likedByCurrentUser = likedByCurrentUser;
    }
}
