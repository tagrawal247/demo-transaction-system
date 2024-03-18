package demo.java.demotransactionsystem.exception;

import demo.java.demotransactionsystem.model.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import lombok.extern.slf4j.*;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static demo.java.demotransactionsystem.exception.ErrorMessage.INTERNAL_SERVER_ERROR_MESSAGE;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler{

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleCustomValidationExceptions(ValidationException exception){
        return new ResponseEntity<>(mapToErrorResponse(exception.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleCustomAuthorizationError(UnauthorizedException exception){
        return new ResponseEntity<>(mapToErrorResponse(exception.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({MalformedJwtException.class, ExpiredJwtException.class})
    public ResponseEntity<ErrorResponse> malformedJwtExceptions(MalformedJwtException exception){
        return new ResponseEntity<>(mapToErrorResponse(exception.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorResponse> argumentNotValid(MethodArgumentNotValidException exception){
        var errorList=  exception.getBindingResult().getFieldErrors();
        var errorMessage = new StringBuilder("Invalid Request - ");
        errorList.stream().forEach(fieldError -> errorMessage.append("Field Name: " + fieldError.getField() + " - Error Message: " + fieldError.getDefaultMessage()));
        return new ResponseEntity<>(mapToErrorResponse(errorMessage.toString()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception exception){
        log.error(exception.getMessage());
        return new ResponseEntity<>(mapToErrorResponse(INTERNAL_SERVER_ERROR_MESSAGE), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ErrorResponse mapToErrorResponse(String message){
        var errorResponse = new ErrorResponse();
        errorResponse.setError(message);
        return errorResponse;
    }
}