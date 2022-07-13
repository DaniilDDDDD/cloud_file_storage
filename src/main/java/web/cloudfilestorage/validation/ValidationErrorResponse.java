package web.cloudfilestorage.validation;

import java.util.List;

public class ValidationErrorResponse {
    private final List<Violation> violations;

    public ValidationErrorResponse(List<Violation> violations) {
        this.violations = violations;
    }

    public ValidationErrorResponse(Violation violation) {
        this.violations = List.of(violation);
    }

    public List<Violation> getViolations() {
        return violations;
    }
}
