package tlm.dev.lectern.site.data;

public final class DataFormatException extends RuntimeException {

    public DataFormatException(String file, String problem) {
        super(file + ": " + problem);
    }

    public DataFormatException(String file, String problem, Throwable cause) {
        super(file + ": " + problem, cause);
    }
}
