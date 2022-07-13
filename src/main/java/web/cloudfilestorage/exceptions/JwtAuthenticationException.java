package web.cloudfilestorage.exceptions;

import javax.naming.AuthenticationException;

public class JwtAuthenticationException extends AuthenticationException {

    private String field;

    public JwtAuthenticationException(String message, String field) {
        super(message);
        this.field = field;
    }

    public JwtAuthenticationException() {
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
}
