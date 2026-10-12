package tlm.dev.lectern.site;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import tlm.dev.lectern.site.model.ContactMethod;

record CommandLine(Path data, Path out, Optional<ContactMethod> contact, boolean publicBuild) {

    static final String USAGE =
            "usage: LecternSite --data <LecternData clone> [--out <folder>] [--contact <email or https URL>] [--public]";
    static final String DEFAULT_OUT = "build/site";

    private static final String DATA = "--data";
    private static final String OUT = "--out";
    private static final String CONTACT = "--contact";
    private static final String PUBLIC = "--public";
    private static final List<String> VALUED_OPTIONS = List.of(DATA, OUT, CONTACT);

    static CommandLine parse(String[] args) {
        Map<String, String> options = new TreeMap<>();
        boolean publicBuild = false;
        for (int index = 0; index < args.length; index++) {
            String argument = args[index];
            if (argument.equals(PUBLIC)) {
                if (publicBuild) {
                    throw new UsageException("option " + PUBLIC + " is given twice");
                }
                publicBuild = true;
            } else if (VALUED_OPTIONS.contains(argument)) {
                if (index + 1 >= args.length || args[index + 1].startsWith("--")) {
                    throw new UsageException("option " + argument + " needs a value");
                }
                if (options.putIfAbsent(argument, args[++index]) != null) {
                    throw new UsageException("option " + argument + " is given twice");
                }
            } else if (argument.startsWith("--")) {
                throw new UsageException("unknown option " + argument);
            } else {
                throw new UsageException("unexpected argument " + argument);
            }
        }
        if (!options.containsKey(DATA)) {
            throw new UsageException("missing option " + DATA);
        }
        Optional<ContactMethod> contact = Optional.ofNullable(options.get(CONTACT)).map(CommandLine::contact);
        if (publicBuild && contact.isEmpty()) {
            throw new UsageException("option " + PUBLIC + " needs " + CONTACT + ": a public site must say how to reach the person responsible");
        }
        return new CommandLine(
                path(options.get(DATA)),
                path(options.getOrDefault(OUT, DEFAULT_OUT)),
                contact,
                publicBuild);
    }

    private static Path path(String text) {
        try {
            return Path.of(text);
        } catch (InvalidPathException e) {
            throw new UsageException("not a usable folder path: " + text, e);
        }
    }

    private static ContactMethod contact(String text) {
        try {
            return new ContactMethod(text);
        } catch (IllegalArgumentException e) {
            throw new UsageException("invalid " + CONTACT + ": " + e.getMessage(), e);
        }
    }
}
