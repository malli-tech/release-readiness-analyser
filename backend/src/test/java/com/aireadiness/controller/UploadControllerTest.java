package com.aireadiness.controller;

import com.aireadiness.dto.upload.UploadResponse;
import com.aireadiness.exception.InvalidArchiveException;
import com.aireadiness.exception.ResourceNotFoundException;
import com.aireadiness.service.JwtService;
import com.aireadiness.service.UploadService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UploadService uploadService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    private void mockAuth(String token, String email) {
        UserDetails userDetails = new User(email, "password", Collections.emptyList());
        when(jwtService.extractUsername(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtService.isTokenValid(eq(token), any(UserDetails.class))).thenReturn(true);
    }

    private byte[] createSampleZip(String entryName, String content) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry(entryName);
            zos.putNextEntry(entry);
            zos.write(content.getBytes());
            zos.closeEntry();
        }
        return baos.toByteArray();
    }

    @Test
    @DisplayName("1. Valid ZIP upload (COMPLETE_PROJECT) should return 201 Created and READY status")
    public void testValidZipUploadComplete() throws Exception {
        String token = "valid.token";
        mockAuth(token, "student@univ.edu");

        byte[] zipBytes = createSampleZip("src/App.java", "public class App {}");
        MockMultipartFile file = new MockMultipartFile("file", "project.zip", "application/zip", zipBytes);

        UploadResponse response = new UploadResponse(
                "upl-1", "rel-1", "COMPLETE_PROJECT", "project.zip",
                zipBytes.length, 1, "READY", Instant.now(),
                "Project uploaded and extracted safely to isolated workspace."
        );

        when(uploadService.uploadProject(eq("rel-1"), any(), eq("COMPLETE_PROJECT"))).thenReturn(response);

        mockMvc.perform(multipart("/api/releases/rel-1/upload")
                        .file(file)
                        .param("uploadMode", "COMPLETE_PROJECT")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uploadId").value("upl-1"))
                .andExpect(jsonPath("$.uploadMode").value("COMPLETE_PROJECT"))
                .andExpect(jsonPath("$.status").value("READY"))
                .andExpect(jsonPath("$.fileCount").value(1));
    }

    @Test
    @DisplayName("2. Valid ZIP upload (SELECTED_CONTENT) should return 201 Created")
    public void testValidZipUploadSelectedContent() throws Exception {
        String token = "valid.token";
        mockAuth(token, "student@univ.edu");

        byte[] zipBytes = createSampleZip("src/Service.java", "public class Service {}");
        MockMultipartFile file = new MockMultipartFile("file", "selected.zip", "application/zip", zipBytes);

        UploadResponse response = new UploadResponse(
                "upl-2", "rel-1", "SELECTED_CONTENT", "selected.zip",
                zipBytes.length, 1, "READY", Instant.now(),
                "Project uploaded and extracted safely to isolated workspace."
        );

        when(uploadService.uploadProject(eq("rel-1"), any(), eq("SELECTED_CONTENT"))).thenReturn(response);

        mockMvc.perform(multipart("/api/releases/rel-1/upload")
                        .file(file)
                        .param("uploadMode", "SELECTED_CONTENT")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uploadMode").value("SELECTED_CONTENT"))
                .andExpect(jsonPath("$.status").value("READY"));
    }

    @Test
    @DisplayName("3. Non-ZIP file disguised as .zip should return 400 Bad Request")
    public void testNonZipFileDisguised() throws Exception {
        String token = "valid.token";
        mockAuth(token, "student@univ.edu");

        byte[] fakeBytes = "This is not a zip file".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "fake.zip", "application/zip", fakeBytes);

        when(uploadService.uploadProject(eq("rel-1"), any(), any()))
                .thenThrow(new InvalidArchiveException("Invalid archive contents: file is not a valid ZIP archive."));

        mockMvc.perform(multipart("/api/releases/rel-1/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid archive contents: file is not a valid ZIP archive."));
    }

    @Test
    @DisplayName("4. Malformed ZIP archive should return 400 Bad Request")
    public void testMalformedZip() throws Exception {
        String token = "valid.token";
        mockAuth(token, "student@univ.edu");

        byte[] badBytes = new byte[]{0x50, 0x4B, 0x03, 0x04, 0x00, 0x00, 0x12};
        MockMultipartFile file = new MockMultipartFile("file", "corrupt.zip", "application/zip", badBytes);

        when(uploadService.uploadProject(eq("rel-1"), any(), any()))
                .thenThrow(new InvalidArchiveException("Invalid archive contents."));

        mockMvc.perform(multipart("/api/releases/rel-1/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid archive contents."));
    }

    @Test
    @DisplayName("5. Path traversal attempt in ZIP entry should be rejected with 400 Bad Request")
    public void testPathTraversalZipRejected() throws Exception {
        String token = "valid.token";
        mockAuth(token, "student@univ.edu");

        byte[] zipBytes = createSampleZip("../../etc/passwd", "root:x:0:0");
        MockMultipartFile file = new MockMultipartFile("file", "evil.zip", "application/zip", zipBytes);

        when(uploadService.uploadProject(eq("rel-1"), any(), any()))
                .thenThrow(new InvalidArchiveException("Invalid archive contents."));

        mockMvc.perform(multipart("/api/releases/rel-1/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid archive contents."));
    }

    @Test
    @DisplayName("6. Unauthenticated upload request should return 403 Forbidden")
    public void testUnauthenticatedUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "project.zip", "application/zip", new byte[]{0x50, 0x4B});

        mockMvc.perform(multipart("/api/releases/rel-1/upload")
                        .file(file))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("7. Unauthorized upload to another user's release should return 404 Not Found")
    public void testUnauthorizedReleaseUpload() throws Exception {
        String token = "valid.token";
        mockAuth(token, "otheruser@univ.edu");

        byte[] zipBytes = createSampleZip("src/App.java", "code");
        MockMultipartFile file = new MockMultipartFile("file", "project.zip", "application/zip", zipBytes);

        when(uploadService.uploadProject(eq("other-rel"), any(), any()))
                .thenThrow(new ResourceNotFoundException("Release not found with id: other-rel"));

        mockMvc.perform(multipart("/api/releases/other-rel/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("8. Get latest upload metadata should return 200 OK")
    public void testGetLatestUpload() throws Exception {
        String token = "valid.token";
        mockAuth(token, "student@univ.edu");

        UploadResponse response = new UploadResponse(
                "upl-1", "rel-1", "COMPLETE_PROJECT", "project.zip",
                1024, 15, "READY", Instant.now(), "Upload metadata retrieved."
        );

        when(uploadService.getLatestUpload("rel-1")).thenReturn(response);

        mockMvc.perform(get("/api/releases/rel-1/upload")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadId").value("upl-1"))
                .andExpect(jsonPath("$.status").value("READY"))
                .andExpect(jsonPath("$.fileCount").value(15));
    }
}
