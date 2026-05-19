package com.esprit.campconnect.Formation.service;

import com.cloudinary.Cloudinary;
import com.esprit.campconnect.Formation.dto.FormationMediaResponseDto;
import com.esprit.campconnect.Formation.entity.Formation;
import com.esprit.campconnect.Formation.entity.FormationMedia;
import com.esprit.campconnect.Formation.entity.FormationMediaType;
import com.esprit.campconnect.Formation.repository.FormationMediaRepository;
import com.esprit.campconnect.Formation.repository.FormationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FormationMediaServiceImpl implements FormationMediaService {

    private final FormationRepository formationRepository;
    private final FormationMediaRepository formationMediaRepository;
    private final Cloudinary cloudinary;

    @Override
    @Transactional
    public FormationMediaResponseDto addMedia(Long formationId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le fichier est obligatoire");
        }

        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Formation introuvable"));

        FormationMediaType mediaType = resolveMediaType(file.getContentType());
        if (mediaType == null) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Type de fichier non supporte. Utiliser image/* ou video/*");
        }

        Map<?, ?> uploadResult = uploadToCloudinary(file, formationId);
        String mediaUrl = uploadResult.get("secure_url") != null ? uploadResult.get("secure_url").toString() : null;
        String mediaPublicId = uploadResult.get("public_id") != null ? uploadResult.get("public_id").toString() : null;
        if (mediaUrl == null || mediaPublicId == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Reponse Cloudinary invalide");
        }

        Integer maxDisplayOrder = formationMediaRepository.getMaxDisplayOrderForFormation(formationId);

        FormationMedia media = new FormationMedia();
        media.setFormation(formation);
        media.setMediaType(mediaType);
        media.setMediaUrl(mediaUrl);
        media.setMediaPublicId(mediaPublicId);
        media.setFileName(file.getOriginalFilename());
        media.setMimeType(file.getContentType());
        media.setFileSize(file.getSize());
        media.setDisplayOrder((maxDisplayOrder == null ? -1 : maxDisplayOrder) + 1);

        FormationMedia savedMedia = formationMediaRepository.save(media);
        return mapToDto(savedMedia);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FormationMediaResponseDto> getMediaByFormation(Long formationId) {
        if (!formationRepository.existsById(formationId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Formation introuvable");
        }

        return formationMediaRepository.findByFormation_IdOrderByDisplayOrderAscIdAsc(formationId).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteMedia(Long formationId, Long mediaId) {
        FormationMedia media = formationMediaRepository.findByIdAndFormation_Id(mediaId, formationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Media introuvable pour cette formation"));

        String resourceType = media.getMediaType() == FormationMediaType.VIDEO ? "video" : "image";
        deleteFromCloudinary(media.getMediaPublicId(), resourceType);
        formationMediaRepository.delete(media);
    }

    private FormationMediaResponseDto mapToDto(FormationMedia media) {
        FormationMediaResponseDto dto = new FormationMediaResponseDto();
        dto.setId(media.getId());
        dto.setFormationId(media.getFormationId());
        dto.setMediaUrl(media.getMediaUrl());
        dto.setMediaType(media.getMediaType());
        dto.setFileName(media.getFileName());
        dto.setMimeType(media.getMimeType());
        dto.setFileSize(media.getFileSize());
        dto.setDisplayOrder(media.getDisplayOrder());
        dto.setUploadDate(media.getUploadDate());
        return dto;
    }

    private FormationMediaType resolveMediaType(String mimeType) {
        if (mimeType == null || mimeType.isBlank()) {
            return null;
        }
        if (mimeType.startsWith("image/")) {
            return FormationMediaType.IMAGE;
        }
        if (mimeType.startsWith("video/")) {
            return FormationMediaType.VIDEO;
        }
        return null;
    }

    private Map<?, ?> uploadToCloudinary(MultipartFile file, Long formationId) {
        try {
            Map<String, Object> uploadOptions = new HashMap<>();
            uploadOptions.put("resource_type", "auto");
            uploadOptions.put("folder", "campconnect/formations/" + formationId);
            return cloudinary.uploader().upload(file.getBytes(), uploadOptions);
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Echec upload media Cloudinary", exception);
        }
    }

    private void deleteFromCloudinary(String publicId, String resourceType) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }

        try {
            Map<String, Object> destroyOptions = new HashMap<>();
            destroyOptions.put("resource_type", resourceType);
            cloudinary.uploader().destroy(publicId, destroyOptions);
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Echec suppression media Cloudinary", exception);
        }
    }
}
