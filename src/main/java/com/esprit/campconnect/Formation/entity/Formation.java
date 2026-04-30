package com.esprit.campconnect.Formation.entity;

import com.esprit.campconnect.User.Entity.Utilisateur;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "formation")
public class Formation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(nullable = false, length = 4000)
    private String description;

    private LocalDateTime dateCreation;

    private String auteurEmail;
    private String auteurNom;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private FormationLevel level;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private FormationStatus status;

    @Column
    private Integer duration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guide_id", nullable = false)
    @JsonIgnore
    private Utilisateur guide;

    @PrePersist
    protected void onCreate() {
        if (this.dateCreation == null) {
            this.dateCreation = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = FormationStatus.DRAFT;
        }
        if (this.level == null) {
            this.level = FormationLevel.BEGINNER;
        }
        if (this.duration == null || this.duration <= 0) {
            this.duration = 60;
        }
    }

    @Transient
    public Long getGuideId() {
        return guide != null ? guide.getId() : null;
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

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
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

    public FormationStatus getStatus() {
        return status;
    }

    public void setStatus(FormationStatus status) {
        this.status = status;
    }

    public FormationLevel getLevel() {
        return level;
    }

    public void setLevel(FormationLevel level) {
        this.level = level;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Utilisateur getGuide() {
        return guide;
    }

    public void setGuide(Utilisateur guide) {
        this.guide = guide;
    }
}
