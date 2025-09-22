package co.edu.itm.infra.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/invoice-mapping/api/test");
    }

    @Test
    void handleNotFound_returns404WithProblemDetail() {
        ResponseEntity<ProblemDetail> resp = handler.handleNotFound(new NoSuchElementException("Not here"), request);
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        assertEquals("Resource not found", resp.getBody().getTitle());
        assertEquals("Not here", resp.getBody().getDetail());
        assertEquals("/invoice-mapping/api/test", resp.getBody().getProperties().get("path"));
    }

    @Test
    void handleValidation_returns400WithErrorsMap() throws Exception {
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Object(), "target");
        binding.addError(new FieldError("target", "name", "must not be blank"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, binding);

        ResponseEntity<ProblemDetail> resp = handler.handleValidation(ex, request);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("Validation failed", resp.getBody().getTitle());
        assertTrue(((java.util.Map<?,?>) resp.getBody().getProperties().get("errors")).containsKey("name"));
    }

    @Test
    void handleConstraintViolation_returns400() {
        ConstraintViolationException ex = new ConstraintViolationException("invalid", java.util.Set.of());
        ResponseEntity<ProblemDetail> resp = handler.handleConstraintViolation(ex, request);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("Constraint violation", resp.getBody().getTitle());
    }

    @Test
    void handleConflict_returns409() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("duplicate");
        ResponseEntity<ProblemDetail> resp = handler.handleConflict(ex, request);
        assertEquals(HttpStatus.CONFLICT, resp.getStatusCode());
        assertEquals("Data integrity violation", resp.getBody().getTitle());
    }

    @Test
    void handleMalformed_returns400() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("bad json");
        ResponseEntity<ProblemDetail> resp = handler.handleMalformed(ex, request);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("Malformed JSON request", resp.getBody().getTitle());
    }

    @Test
    void handleGeneric_returns500() {
        ResponseEntity<ProblemDetail> resp = handler.handleGeneric(new RuntimeException("oops"), request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        assertEquals("Unexpected error", resp.getBody().getTitle());
    }
}
