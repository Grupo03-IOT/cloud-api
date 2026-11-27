package com.pe.cloudapi.iam.interfaces.rest;

import com.pe.cloudapi.iam.application.internal.ports.in.ListDemoRequestsUseCase;
import com.pe.cloudapi.iam.application.internal.ports.in.RequestDemoUseCase;
import com.pe.cloudapi.iam.interfaces.rest.resources.DemoRequestReceiptResource;
import com.pe.cloudapi.iam.interfaces.rest.resources.DemoRequestResource;
import com.pe.cloudapi.iam.interfaces.rest.resources.RequestDemoResource;
import com.pe.cloudapi.iam.interfaces.rest.transform.DemoRequestResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

/**
 * Solicitudes de demostración (US06): cualquiera las envía desde el Landing
 * Page y solo un administrador las lee.
 */
@Tag(name = "Demo requests", description = "Requests sent from the landing page")
@RestController
@RequestMapping(value = "/api/v1/demo-requests", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class DemoRequestsController {

    private final RequestDemoUseCase requestDemo;
    private final ListDemoRequestsUseCase listRequests;
    private final DemoRequestResourceAssembler assembler;

    /**
     * Público y limitado por IP. El {@code @CrossOrigin} suma el origen del
     * Landing Page solo a este método; el resto de la API sigue con
     * {@code app.cors.allowed-origins}.
     *
     * <p>Si el campo trampa llega relleno se responde igual que a una solicitud
     * válida, para no avisar al robot, y no se guarda nada.
     */
    @CrossOrigin(origins = "${app.cors.public-form-origins:https://grupo03-iot.github.io}",
            allowedHeaders = "Content-Type", maxAge = 3600)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirements
    @Operation(summary = "Request a product demo",
            description = "Public. Stores the request. Limited per client IP.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Request received"),
            @ApiResponse(responseCode = "400", description = "Missing or invalid field, or consent not given"),
            @ApiResponse(responseCode = "429", description = "IAM_DEMO_REQUEST_LIMIT_REACHED")
    })
    public ResponseEntity<DemoRequestReceiptResource> request(
            @Valid @RequestBody RequestDemoResource resource) {
        DemoRequestReceiptResource receipt = resource.isHoneypotFilled()
                ? new DemoRequestReceiptResource(UUID.randomUUID(), OffsetDateTime.now(ZoneOffset.UTC))
                : assembler.toReceipt(requestDemo.execute(assembler.toCommand(resource)));
        return ResponseEntity.status(HttpStatus.CREATED).body(receipt);
    }

    @GetMapping
    @Operation(summary = "List demo requests, newest first")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Requests returned"),
            @ApiResponse(responseCode = "401", description = "No credential"),
            @ApiResponse(responseCode = "403", description = "Only administrators")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public List<DemoRequestResource> list() {
        return listRequests.execute().stream().map(assembler::toResource).toList();
    }
}
