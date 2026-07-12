package com.ramya.transactionrisk.riskrule;

import java.util.List;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/risk-rules")
@Tag(
        name = "Risk Rule Administration",
        description = "View and update configurable transaction-risk rules"
)
@SecurityRequirement(name = "bearerAuth")
public class RiskRuleController {

    private final RiskRuleService riskRuleService;

    public RiskRuleController(
            RiskRuleService riskRuleService
    ) {
        this.riskRuleService = riskRuleService;
    }

    @GetMapping
    @Operation(
            summary = "List risk rules",
            description = "Returns all database-configured risk rules"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Risk rules returned"
    )
    public ResponseEntity<List<RiskRuleResponse>> findAll() {
        return ResponseEntity.ok(riskRuleService.findAll());
    }

    @PutMapping("/{code}")
    @Operation(
            summary = "Update a risk rule",
            description = "Updates status, score, parameter, and explanation for a rule"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Risk rule updated"
    )
    public ResponseEntity<RiskRuleResponse> update(
            @PathVariable RiskRuleCode code,
            @Valid @RequestBody UpdateRiskRuleRequest request
    ) {
        return ResponseEntity.ok(
                riskRuleService.update(code, request)
        );
    }
}
