package com.esprit.campconnect.Formation.dto;

import com.esprit.campconnect.Formation.entity.FormationMediaType;

import java.time.LocalDateTime;

public class FormationMediaResponseDto {

    private Long id;
    private Long formationId;
    private String mediaUrl;
    private FormationMediaType mediaType;
    private String fileName;
    private String mimeType;
    private Long fileSize;
    private Integer displayOrder;
    private LocalDateTime uploadDate;

    public FormationMediaResponseDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFormationId() {
        return formationId;
    }

    public void setFormationId(Long formationId) {
        this.formationId = formationId;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public FormationMediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(FormationMediaType mediaType) {
        this.mediaType = mediaType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }
}
