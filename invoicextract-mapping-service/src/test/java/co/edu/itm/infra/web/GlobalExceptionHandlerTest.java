package co.edu.itm.infra.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
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
    void handleNotFound_returns404WithErrorResponse() {
        ResponseEntity<ErrorResponse> resp = handler.handleNotFound(new NoSuchElementException("Not here"), request);
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(404, resp.getBody().getError().getCode());
        assertEquals("Not here", resp.getBody().getError().getMessage());
    }

    @Test
    void handleValidation_returns400WithMessage() throws Exception {
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Object(), "target");
        binding.addError(new FieldError("target", "name", "must not be blank"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, binding);

        ResponseEntity<ErrorResponse> resp = handler.handleValidation(ex, request);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(400, resp.getBody().getError().getCode());
        assertTrue(resp.getBody().getError().getMessage().contains("Validation failed"));
        assertTrue(resp.getBody().getError().getMessage().contains("name"));
    }

    @Test
    void handleConstraintViolation_returns400() {
        ConstraintViolationException ex = new ConstraintViolationException("invalid", java.util.Set.of());
        ResponseEntity<ErrorResponse> resp = handler.handleConstraintViolation(ex, request);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(400, resp.getBody().getError().getCode());
        assertTrue(resp.getBody().getError().getMessage().contains("invalid"));
    }

    @Test
    void handleConflict_returns409() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("duplicate");
        ResponseEntity<ErrorResponse> resp = handler.handleConflict(ex, request);
        assertEquals(HttpStatus.CONFLICT, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(409, resp.getBody().getError().getCode());
        assertEquals("The request conflicts with existing data", resp.getBody().getError().getMessage());
    }

    @Test
    void handleMalformed_returns400() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("bad json");
        ResponseEntity<ErrorResponse> resp = handler.handleMalformed(ex, request);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(400, resp.getBody().getError().getCode());
        assertEquals("Request body could not be parsed", resp.getBody().getError().getMessage());
    }

    @Test
    void handleGeneric_returns500() {
        ResponseEntity<ErrorResponse> resp = handler.handleGeneric(new RuntimeException("oops"), request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(500, resp.getBody().getError().getCode());
        assertEquals("An unexpected error occurred", resp.getBody().getError().getMessage());
    }
}
