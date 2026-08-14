package dev.beanguard.server.controllers;

import dev.beanguard.server.models.Parameter;
import dev.beanguard.server.models.ParameterName;
import dev.beanguard.server.models.ParameterUpdateRequest;
import dev.beanguard.server.services.ParameterService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/parameters")
@RequiredArgsConstructor
@Tag(name = "Parameters")
public class ParametersController {

    private final ParameterService parameterService;

    @GetMapping
    public List<Parameter> getAllParameters() {
        return parameterService.getAllParameters();
    }

    @PutMapping("/{name}")
    public Parameter updateParameter(@PathVariable String name,
                                     @RequestBody @Valid ParameterUpdateRequest request) {
        return parameterService.updateAndGet(resolve(name), request.value());
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> resetParameter(@PathVariable String name) {
        parameterService.resetParameter(resolve(name));
        return ResponseEntity.noContent().build();
    }

    private ParameterName resolve(String name) {
        try {
            return ParameterName.valueOf(name);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown parameter: " + name);
        }
    }
}
