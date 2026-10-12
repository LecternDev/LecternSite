package tlm.dev.lectern.site.pages;

import java.util.Objects;
import java.util.Optional;
import tlm.dev.lectern.site.model.ContactMethod;

public final class PageFrames {

    public static final String NOTICE =
            "NOT AN OFFICIAL MINECRAFT PRODUCT. NOT APPROVED BY OR ASSOCIATED WITH MOJANG OR MICROSOFT.";

    private final Optional<ContactMethod> contact;

    public PageFrames(Optional<ContactMethod> contact) {
        this.contact = Objects.requireNonNull(contact, "contact");
    }

    public PageFrame frame(String title, int depth) {
        return new PageFrame(
                title,
                SiteUrls.root(depth),
                NOTICE,
                contact.map(ContactMethod::value).orElse(""),
                contact.flatMap(ContactMethod::href).orElse(""));
    }
}
