package com.aireadiness.controller;

import com.aireadiness.dto.report.ReleaseReportResponse;
import com.aireadiness.service.ReleaseReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ReleaseReportController {

    private final ReleaseReportService releaseReportService;

    public ReleaseReportController(ReleaseReportService releaseReportService) {
        this.releaseReportService = releaseReportService;
    }

    @GetMapping("/api/reports")
    public ResponseEntity<List<ReleaseReportResponse>> getAllUserReports() {
        List<ReleaseReportResponse> reports = releaseReportService.getAllUserReports();
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/api/projects/{projectId}/reports")
    public ResponseEntity<List<ReleaseReportResponse>> getProjectReports(@PathVariable String projectId) {
        List<ReleaseReportResponse> reports = releaseReportService.getProjectReports(projectId);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/api/projects/{projectId}/reports/{releaseId}")
    public ResponseEntity<ReleaseReportResponse> getReleaseReport(
            @PathVariable String projectId,
            @PathVariable String releaseId
    ) {
        ReleaseReportResponse report = releaseReportService.getReleaseReport(projectId, releaseId);
        return ResponseEntity.ok(report);
    }
}
