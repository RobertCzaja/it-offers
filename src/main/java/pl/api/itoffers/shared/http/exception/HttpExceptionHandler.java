package pl.api.itoffers.shared.http.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class HttpExceptionHandler {

    private final ObjectMapper mapper;

    public HttpExceptionHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public void handle(Exception e, HttpServletResponse response) throws IOException {
        Map<String, Object> errorDetails = new HashMap<>();

        int httpStatusCode = HttpStatus.FORBIDDEN.value();
        String errorMessage = "authentication_error";
        String errorDetail =  e.getMessage();
        if (e.getCause() instanceof ValidationException) {
            ValidationException validationException = (ValidationException) e.getCause();
            httpStatusCode = validationException.getHttpStatusCode();
            errorMessage = "validation_error";
            errorDetail = validationException.getMessage();
        }

        errorDetails.put("code", errorMessage);
        errorDetails.put("details",errorDetail);
        response.setStatus(httpStatusCode);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        mapper.writeValue(response.getWriter(), errorDetails);
    }
}