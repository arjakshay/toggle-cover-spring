package com.togglecover.auth.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskAssessment {
    private int riskScore;
    private boolean highRisk;
    private String reasons;
    private boolean requiresAdditionalAuth;
    private String suggestedAction;
}