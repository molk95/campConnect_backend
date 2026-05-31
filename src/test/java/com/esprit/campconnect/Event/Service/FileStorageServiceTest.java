package com.esprit.campconnect.Event.Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStorageServiceTest {

    @TempDir
    Path uploadDirectory;

    private FileStorageService service;

    @BeforeEach
    void setUp() {
        service = new FileStorageService();
        ReflectionTestUtils.setField(service, "uploadDir", uploadDirectory.toString());
        ReflectionTestUtils.setField(service, "maxFileSize", 12L);
    }

    @Test
    void storeFileWithBytesWritesAllowedImageUsingUniqueName() throws Exception {
        String storedPath = service.storeFile("banner.PNG", new byte[] {1, 2, 3});

        Path storedFile = Path.of(storedPath);
        assertThat(storedFile)
                .exists()
                .hasParentRaw(uploadDirectory.toAbsolutePath().normalize());
        assertThat(storedFile.getFileName().toString()).endsWith(".PNG");
        assertThat(Files.readAllBytes(storedFile)).containsExactly(1, 2, 3);
    }

    @Test
    void storeMultipartFileUsesProvidedBytesAndValidatesFileMetadata() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "thumbnail.webp",
                "image/webp",
                new byte[] {9, 8, 7}
        );

        String storedPath = service.storeFile(file, new byte[] {4, 5, 6});

        assertThat(Path.of(storedPath)).exists();
        assertThat(Files.readAllBytes(Path.of(storedPath))).containsExactly(4, 5, 6);
    }

    @Test
    void storeFileRejectsEmptyOversizedAndUnsupportedFiles() {
        assertThatThrownBy(() -> service.storeFile("empty.jpg", new byte[0]))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty");

        assertThatThrownBy(() -> service.storeFile("large.jpg", new byte[13]))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maximum allowed size");

        assertThatThrownBy(() -> service.storeFile("script.exe", new byte[] {1}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File type not allowed");
    }

    @Test
    void deleteFileRemovesExistingFileAndIgnoresMissingFile() throws Exception {
        Path file = Files.write(uploadDirectory.resolve("old-image.jpg"), new byte[] {1});

        service.deleteFile(file.toString());
        service.deleteFile(uploadDirectory.resolve("missing.jpg").toString());

        assertThat(file).doesNotExist();
    }
}
