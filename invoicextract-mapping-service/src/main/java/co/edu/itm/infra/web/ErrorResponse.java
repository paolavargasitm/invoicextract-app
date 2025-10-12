package co.edu.itm.infra.web;

public class ErrorResponse {
    private final ErrorBody error;

    public ErrorResponse(int code, String message) {
        this.error = new ErrorBody(code, message);
    }

    public ErrorBody getError() {
        return error;
    }

    public static ErrorResponse of(int code, String message) {
        return new ErrorResponse(code, message);
    }

    public static class ErrorBody {
        private final int code;
        private final String message;

        public ErrorBody(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}
