package match.service.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import match.service.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError>  handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request){
       String errorMessage = ex.getBindingResult().getFieldErrors().stream()
               .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
               .collect(Collectors.joining(", "));
       ApiError apiError = new ApiError(
               400,
               "VALIDATION_FAILED",
               errorMessage,
               LocalDateTime.now(),
               request.getRequestURI()
       );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(ResumeContentNotFoundException.class)
    public ResponseEntity<ApiError> contentNotFound(ResumeContentNotFoundException exception, HttpServletRequest request){
        String path = request.getRequestURI();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(404, "NOT_FOUND", exception.getMessage(), LocalDateTime.now(), path));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> genericExceptions(Exception exception, HttpServletRequest request){
        log.error("Unexpected error", exception);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body( new ApiError(500, "INTERNAL_SERVER_ERROR", "An unexpected error occurred. Please try again later.", LocalDateTime.now(), request.getRequestURI()));
    }

}
