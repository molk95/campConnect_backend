package com.esprit.campconnect.Restauration.Service;
import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RepasCloudinaryService {

    private final Cloudinary cloudinary;

    public Map<String, String> uploadImage(MultipartFile file) {
        try {
            System.out.println("=== REPAS CLOUDINARY ===");
            System.out.println("cloud_name: " + cloudinary.config.cloudName);

            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    Map.of("folder", "campconnect/repas") // 👈 folder séparé pour les repas
            );

            return Map.of(
                    "imageUrl", uploadResult.get("secure_url").toString(),
                    "imagePublicId", uploadResult.get("public_id").toString()
            );
        } catch (Exception e) {
            System.out.println("=== REPAS CLOUDINARY ERROR ===");
            e.printStackTrace();
            throw new RuntimeException("Failed to upload repas image", e);
        }
    }

    public void deleteImage(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, Map.of());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete repas image", e);
        }
    }
}