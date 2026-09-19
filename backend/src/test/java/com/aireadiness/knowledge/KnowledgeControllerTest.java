package com.aireadiness.knowledge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aireadiness.exception.ResourceNotFoundException;
import com.aireadiness.knowledge.dto.CreateDocumentRequest;
import com.aireadiness.knowledge.model.*;
import com.aireadiness.knowledge.service.KnowledgeIngestionService;
import com.aireadiness.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class KnowledgeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KnowledgeIngestionService ingestionService;

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

    private KnowledgeDocument createSampleDocument(String id) {
        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setId(id);
        doc.setTitle("Spring Boot Security Best Practices");
        doc.setSource("https://docs.spring.io/security");
        doc.setSourceType(SourceType.OFFICIAL_DOCUMENTATION);
        doc.setCategory(KnowledgeCategory.SECURITY);
        doc.setTechnology("Spring Boot");
        doc.setVersion("3.x");
        doc.setDescription("Official security baseline.");
        doc.setContent("Ensure CORS, CSRF, and authentication headers are configured.");
        doc.setStatus(KnowledgeDocumentStatus.ACTIVE);
        doc.setChunkCount(2);
        doc.setCreatedAt(Instant.now());
        doc.setUpdatedAt(Instant.now());
        return doc;
    }

    private KnowledgeChunk createSampleChunk(String docId, int index) {
        KnowledgeChunk chunk = new KnowledgeChunk();
        chunk.setId("chk-" + index);
        chunk.setDocumentId(docId);
        chunk.setChunkIndex(index);
        chunk.setTitle("Spring Boot Security Best Practices");
        chunk.setSource("https://docs.spring.io/security");
        chunk.setSourceType(SourceType.OFFICIAL_DOCUMENTATION);
        chunk.setCategory(KnowledgeCategory.SECURITY);
        chunk.setTechnology("Spring Boot");
        chunk.setVersion("3.x");
        chunk.setText("Chunk " + index + " content on security rules.");
        chunk.setCreatedAt(Instant.now());
        return chunk;
    }

    @Test
    @DisplayName("1. POST /api/knowledge/documents creates document (201 Created)")
    public void testCreateDocumentSuccess() throws Exception {
        String token = "valid.jwt.token";
        mockAuth(token, "admin@univ.edu");

        CreateDocumentRequest req = new CreateDocumentRequest();
        req.setTitle("Spring Boot Security Best Practices");
        req.setSource("https://docs.spring.io/security");
        req.setSourceType(SourceType.OFFICIAL_DOCUMENTATION);
        req.setCategory(KnowledgeCategory.SECURITY);
        req.setTechnology("Spring Boot");
        req.setVersion("3.x");
        req.setContent("Ensure CORS, CSRF, and authentication headers are configured.");

        KnowledgeDocument doc = createSampleDocument("doc-100");
        when(ingestionService.ingestDocument(any(CreateDocumentRequest.class))).thenReturn(doc);

        mockMvc.perform(post("/api/knowledge/documents")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("doc-100"))
                .andExpect(jsonPath("$.title").value("Spring Boot Security Best Practices"))
                .andExpect(jsonPath("$.category").value("SECURITY"))
                .andExpect(jsonPath("$.chunkCount").value(2));
    }

    @Test
    @DisplayName("2. GET /api/knowledge/documents returns active documents (200 OK)")
    public void testListActiveDocumentsSuccess() throws Exception {
        String token = "valid.jwt.token";
        mockAuth(token, "user@univ.edu");

        KnowledgeDocument doc = createSampleDocument("doc-101");
        when(ingestionService.listActiveDocuments()).thenReturn(List.of(doc));

        mockMvc.perform(get("/api/knowledge/documents")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("doc-101"));
    }

    @Test
    @DisplayName("3. GET /api/knowledge/documents/{id} returns single document (200 OK)")
    public void testGetDocumentByIdSuccess() throws Exception {
        String token = "valid.jwt.token";
        mockAuth(token, "user@univ.edu");

        KnowledgeDocument doc = createSampleDocument("doc-102");
        when(ingestionService.getDocumentById("doc-102")).thenReturn(doc);

        mockMvc.perform(get("/api/knowledge/documents/doc-102")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("doc-102"))
                .andExpect(jsonPath("$.title").value("Spring Boot Security Best Practices"));
    }

    @Test
    @DisplayName("4. GET /api/knowledge/documents/{id} returns 404 when document not found")
    public void testGetDocumentByIdNotFound() throws Exception {
        String token = "valid.jwt.token";
        mockAuth(token, "user@univ.edu");

        when(ingestionService.getDocumentById("missing-doc"))
                .thenThrow(new ResourceNotFoundException("Knowledge document not found with id: missing-doc"));

        mockMvc.perform(get("/api/knowledge/documents/missing-doc")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("5. GET /api/knowledge/documents/{id}/chunks returns document chunks (200 OK)")
    public void testGetChunksForDocumentSuccess() throws Exception {
        String token = "valid.jwt.token";
        mockAuth(token, "user@univ.edu");

        KnowledgeChunk c0 = createSampleChunk("doc-103", 0);
        KnowledgeChunk c1 = createSampleChunk("doc-103", 1);
        when(ingestionService.getChunksForDocument("doc-103")).thenReturn(List.of(c0, c1));

        mockMvc.perform(get("/api/knowledge/documents/doc-103/chunks")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].chunkIndex").value(0))
                .andExpect(jsonPath("$[1].chunkIndex").value(1));
    }
}
