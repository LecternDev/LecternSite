package tlm.dev.lectern.site.pages;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import tlm.dev.lectern.site.model.ChangeValue;

public final class ChangeValueRenderer {

    private static final String PATH_KEY = "path";
    private static final String NOTHING = "none";

    public String render(String key, ChangeValue value) {
        return render(key, value, false);
    }

    private String render(String key, ChangeValue value, boolean nested) {
        return switch (value) {
            case ChangeValue.Text text -> text.value();
            case ChangeValue.Numeric number -> number.text();
            case ChangeValue.Flag flag -> Boolean.toString(flag.value());
            case ChangeValue.Nothing _ -> NOTHING;
            case ChangeValue.Items items -> items(key, items.items(), nested);
            case ChangeValue.Fields fields -> fields(fields.fields(), nested);
        };
    }

    private String items(String key, List<ChangeValue> items, boolean nested) {
        if (items.isEmpty()) {
            return NOTHING;
        }
        String separator = key.equals(PATH_KEY) ? " " : ", ";
        String joined = items.stream().map(item -> render("", item, true)).collect(Collectors.joining(separator));
        return nested ? "[" + joined + "]" : joined;
    }

    private String fields(Map<String, ChangeValue> fields, boolean nested) {
        if (fields.isEmpty()) {
            return NOTHING;
        }
        String joined = fields.entrySet().stream()
                .map(field -> field.getKey() + "=" + render(field.getKey(), field.getValue(), true))
                .collect(Collectors.joining(", "));
        return nested ? "{" + joined + "}" : joined;
    }
}
