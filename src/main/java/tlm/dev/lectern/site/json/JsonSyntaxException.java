package tlm.dev.lectern.site.json;

public final class JsonSyntaxException extends RuntimeException {

    public JsonSyntaxException(String message) {
        super(message);
    }

    public JsonSyntaxException(String message, Throwable cause) {
        super(message, cause);
    }
}
