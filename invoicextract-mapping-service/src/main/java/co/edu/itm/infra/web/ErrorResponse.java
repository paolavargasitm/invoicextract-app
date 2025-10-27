package co.edu.itm.infra.web;

public class ErrorResponse {
    private final ErrorBody error;
    private final java.util.List<ErrorBody.FieldErrorDetail> fieldErrors;
    private final java.util.List<String> violations;

    public ErrorResponse(int code, String message) {
        this.error = new ErrorBody(code, null, message, null);
        this.fieldErrors = null;
        this.violations = null;
    }

    private ErrorResponse(ErrorBody body) {
        this.error = body;
        this.fieldErrors = body.getFieldErrors();
        this.violations = body.getViolations();
    }

    public ErrorBody getError() {
        return error;
    }

    public static ErrorResponse of(int code, String message) {
        return new ErrorResponse(code, message);
    }

    public static ErrorResponse of(org.springframework.http.HttpStatus status, String message, jakarta.servlet.http.HttpServletRequest req) {
        String path = req != null ? req.getRequestURI() : null;
        String reason = status != null ? status.getReasonPhrase() : null;
        String ts = java.time.Instant.now().toString();
        return new ErrorResponse(new ErrorBody(status == null ? 500 : status.value(), reason, message, path, ts, null, null));
    }

    public static ErrorResponse ofWithDetails(org.springframework.http.HttpStatus status, String message,
                                              jakarta.servlet.http.HttpServletRequest req,
                                              java.util.List<ErrorBody.FieldErrorDetail> fieldErrors,
                                              java.util.List<String> violations) {
        String path = req != null ? req.getRequestURI() : null;
        String reason = status != null ? status.getReasonPhrase() : null;
        String ts = java.time.Instant.now().toString();
        return new ErrorResponse(new ErrorBody(status == null ? 400 : status.value(), reason, message, path, ts, fieldErrors, violations));
    }

    public static class ErrorBody {
        private final int code;
        private final String error;
        private final String message;
        private final String path;
        private final String timestamp;
        private final java.util.List<FieldErrorDetail> fieldErrors;
        private final java.util.List<String> violations;

        public ErrorBody(int code, String error, String message, String path) {
            this(code, error, message, path, java.time.Instant.now().toString(), null, null);
        }

        public ErrorBody(int code, String error, String message, String path, String timestamp,
                         java.util.List<FieldErrorDetail> fieldErrors,
                         java.util.List<String> violations) {
            this.code = code;
            this.error = error;
            this.message = message;
            this.path = path;
            this.timestamp = timestamp;
            this.fieldErrors = fieldErrors;
            this.violations = violations;
        }

        public int getCode() {
            return code;
        }

        public String getError() {
            return error;
        }

        public String getMessage() {
            return message;
        }

        public String getPath() {
            return path;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public java.util.List<FieldErrorDetail> getFieldErrors() {
            return fieldErrors;
        }

        public java.util.List<String> getViolations() {
            return violations;
        }

        public static class FieldErrorDetail {
            private final String field;
            private final String message;

            public FieldErrorDetail(String field, String message) {
                this.field = field;
                this.message = message;
            }

            public String getField() {
                return field;
            }

            public String getMessage() {
                return message;
            }
        }
    }

    public java.util.List<ErrorBody.FieldErrorDetail> getFieldErrors() {
        return fieldErrors;
    }

    public java.util.List<String> getViolations() {
        return violations;
    }
}
