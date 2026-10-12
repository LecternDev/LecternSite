package tlm.dev.lectern.site;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import tlm.dev.lectern.site.data.DataFormatException;
import tlm.dev.lectern.site.pages.PageWriteException;

public final class Main {

    static final int EXIT_OK = 0;
    static final int EXIT_FAILURE = 1;
    static final int EXIT_USAGE = 2;

    private Main() {
    }

    public static void main(String[] args) {
        System.exit(run(args, System.out, System.err));
    }

    static int run(String[] args, PrintStream out, PrintStream err) {
        try {
            CommandLine command = CommandLine.parse(args);
            int pages = Composition.generator(command).generate();
            print(out, "Wrote " + pages + " pages to " + command.out());
            return EXIT_OK;
        } catch (UsageException e) {
            print(err, "error: " + e.getMessage() + "\n" + CommandLine.USAGE);
            return EXIT_USAGE;
        } catch (DataFormatException | PageWriteException e) {
            print(err, "error: " + e.getMessage());
            return EXIT_FAILURE;
        }
    }

    private static void print(PrintStream stream, String message) {
        stream.writeBytes((message + "\n").getBytes(StandardCharsets.UTF_8));
        stream.flush();
    }
}
