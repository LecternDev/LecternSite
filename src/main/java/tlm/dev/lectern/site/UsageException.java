package tlm.dev.lectern.site;

final class UsageException extends RuntimeException {

    UsageException(String message) {
        super(message);
    }

    UsageException(String message, Throwable cause) {
        super(message, cause);
    }
}
