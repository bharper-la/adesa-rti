package com.la.dataservices.adesa_rti;

import com.la.dataservices.adesa_rti.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final AlertService alerts;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAny(Exception ex, HttpServletRequest req) {
        String subject = "[adesa-rti] ERROR " + ex.getClass().getSimpleName();
        String body = """
        Path: %s
        Method: %s
        Message: %s
        """.formatted(req.getRequestURI(), req.getMethod(), ex.getMessage());
        alerts.sendError(subject, body);    // fire-and-forget
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Something went wrong; reference your logs.");
    }
}

