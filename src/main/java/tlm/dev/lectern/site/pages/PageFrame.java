package tlm.dev.lectern.site.pages;

public record PageFrame(String title, String root, String notice, String contactText, String contactHref) {

    public String home() {
        return root.isEmpty() ? "./" : root;
    }

    public boolean hasContact() {
        return !contactText.isEmpty();
    }

    public boolean hasContactLink() {
        return !contactHref.isEmpty();
    }
}
