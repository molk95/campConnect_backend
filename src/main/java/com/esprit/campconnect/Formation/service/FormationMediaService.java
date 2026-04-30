package com.esprit.campconnect.Formation.service;

import com.esprit.campconnect.Formation.dto.FormationMediaResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FormationMediaService {

    FormationMediaResponseDto addMedia(Long formationId, MultipartFile file);

    List<FormationMediaResponseDto> getMediaByFormation(Long formationId);

    void deleteMedia(Long formationId, Long mediaId);
}
