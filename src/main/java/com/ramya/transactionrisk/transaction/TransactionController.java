package com.ramya.transactionrisk.transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
@Tag(
        name = "Transactions",
        description = "Submit, search, and retrieve explainable risk assessments"
)
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(
            TransactionService transactionService
    ) {
        this.transactionService = transactionService;
    }

    @PostMapping
    @Operation(
            summary = "Submit a transaction",
            description = "Validates, evaluates, and persists a transaction with its risk assessment"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Transaction created"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid transaction request",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content
            )
    })
    public ResponseEntity<TransactionResponse> create(
            @Valid @RequestBody CreateTransactionRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transactionService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Retrieve a transaction",
            description = "Returns a transaction and its explainable risk assessment"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Transaction found"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Transaction not found",
                    content = @Content
            )
    })
    public ResponseEntity<TransactionResponse> getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                transactionService.getById(id)
        );
    }

    @GetMapping
    @Operation(
            summary = "Search transactions",
            description = "Returns filtered and paginated transaction risk assessments"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Search completed"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid search criteria",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content
            )
    })
    public ResponseEntity<Page<TransactionResponse>> search(
            @RequestParam(required = false)
            String customerId,

            @RequestParam(required = false)
            RiskLevel riskLevel,

            @RequestParam(required = false)
            String country,

            @RequestParam(required = false)
            BigDecimal minAmount,

            @RequestParam(required = false)
            BigDecimal maxAmount,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant to,

            @ParameterObject
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                transactionService.search(
                        customerId,
                        riskLevel,
                        country,
                        minAmount,
                        maxAmount,
                        from,
                        to,
                        pageable
                )
        );
    }
}
