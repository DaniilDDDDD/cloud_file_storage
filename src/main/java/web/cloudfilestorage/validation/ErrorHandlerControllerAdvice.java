package web.cloudfilestorage.validation;

import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import web.cloudfilestorage.exceptions.JwtAuthenticationException;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.servlet.ServletException;
import javax.validation.ConstraintViolationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class ErrorHandlerControllerAdvice {

    @ExceptionHandler({
            MethodArgumentNotValidException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ValidationErrorResponse onConstraintValidationException(
            MethodArgumentNotValidException e
    ) {
        e.printStackTrace();
        final Violation violation = new Violation(
                e.getParameter().getParameterName(),
                e.getMessage()
        );
        return new ValidationErrorResponse(violation);
    }

    @ExceptionHandler({
            ConstraintViolationException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ValidationErrorResponse onConstraintValidationException(
            ConstraintViolationException e
    ) {
        final List<Violation> violations = e.getConstraintViolations().stream()
                .map(
                        violation -> new Violation(
                                violation.getPropertyPath().toString(),
                                violation.getMessage()
                        )
                )
                .collect(Collectors.toList());
        return new ValidationErrorResponse(violations);
    }

    @ExceptionHandler({
            SQLException.class,
            IOException.class,
            BadCredentialsException.class,
            EntityNotFoundException.class,
            EntityExistsException.class,
            FileNotFoundException.class,
            ServletException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ValidationErrorResponse onDataIntegrityViolationExceptionAndIOException(
            Exception e
    ) {
        final Violation violation = new Violation(
                "",
                e.getMessage()
        );
        return new ValidationErrorResponse(violation);
    }

    @ExceptionHandler({
            AccessDeniedException.class
    })
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public ValidationErrorResponse onAccessException(
            Exception e
    ) {
        final Violation violation = new Violation(
                "Authorization",
                e.getMessage()
        );
        return new ValidationErrorResponse(violation);
    }

    @ExceptionHandler({
            JwtException.class,
            AuthenticationException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ValidationErrorResponse onUsernameNotFoundException(
            Exception e
    ) {
        final Violation violation = new Violation(
                "login",
                e.getMessage()
        );
        return new ValidationErrorResponse(violation);
    }

    @ExceptionHandler({
            JwtAuthenticationException.class
    })
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public ValidationErrorResponse onAuthenticationException(
            JwtAuthenticationException e
    ) {
        final Violation violation = new Violation(
                e.getField(),
                e.getMessage()
        );
        return new ValidationErrorResponse(violation);
    }

    @ExceptionHandler({
            NoSuchAlgorithmException.class,
            InvalidKeySpecException.class
    })
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ValidationErrorResponse onAuthenticationException(
            GeneralSecurityException e
    ) {
        final Violation violation = new Violation(
                "",
                "Token generation error!"
        );
        return new ValidationErrorResponse(violation);
    }

    @ExceptionHandler({
            AssertionError.class
    })
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ValidationErrorResponse onAuthenticationException(
            Exception e
    ) {
        final Violation violation = new Violation(
                "",
                "Internal server error!"
        );
        return new ValidationErrorResponse(violation);
    }

}
