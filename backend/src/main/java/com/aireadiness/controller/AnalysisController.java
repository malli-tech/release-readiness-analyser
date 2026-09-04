package com.aireadiness.controller;

import com.aireadiness.dto.analysis.AnalysisResponse;
import com.aireadiness.service.AnalysisService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/api/releases/{releaseId}/analysis")
    public ResponseEntity<AnalysisResponse> startAnalysis(@PathVariable String releaseId) {
        AnalysisResponse response = analysisService.startAnalysis(releaseId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/releases/{releaseId}/analysis")
    public ResponseEntity<AnalysisResponse> getLatestAnalysisForRelease(@PathVariable String releaseId) {
        AnalysisResponse response = analysisService.getLatestAnalysisForRelease(releaseId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/analyses/{analysisId}")
    public ResponseEntity<AnalysisResponse> getAnalysisById(@PathVariable String analysisId) {
        AnalysisResponse response = analysisService.getAnalysisById(analysisId);
        return ResponseEntity.ok(response);
    }
}
