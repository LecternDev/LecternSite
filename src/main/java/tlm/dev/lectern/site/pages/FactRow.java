package tlm.dev.lectern.site.pages;

public record FactRow(String label, String text, String href) {

    static FactRow plain(String label, String text) {
        return new FactRow(label, text, "");
    }

    public boolean hasLink() {
        return !href.isEmpty();
    }
}
