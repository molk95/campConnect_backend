package com.esprit.campconnect.Formation.entity.guide;

import com.esprit.campconnect.Formation.entity.Formation;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "guide_interactif")
public class GuideInteractif {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 180)
    private String titre;

    @Column(nullable = false, length = 3000)
    private String description;

    @Column(nullable = false, length = 300)
    private String recompenseFinale;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formation_id", nullable = false, unique = true)
    @JsonIgnore
    private Formation formation;

    @OneToMany(mappedBy = "guide", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<GuideStep> steps = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
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

    public String getRecompenseFinale() {
        return recompenseFinale;
    }

    public void setRecompenseFinale(String recompenseFinale) {
        this.recompenseFinale = recompenseFinale;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Formation getFormation() {
        return formation;
    }

    public void setFormation(Formation formation) {
        this.formation = formation;
    }

    public List<GuideStep> getSteps() {
        return steps;
    }

    public void setSteps(List<GuideStep> steps) {
        this.steps = steps;
    }
}
