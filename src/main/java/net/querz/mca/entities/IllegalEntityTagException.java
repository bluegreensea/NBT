package net.querz.mca.entities;

import net.querz.nbt.tag.Tag;

public class IllegalEntityTagException extends IllegalArgumentException {
    private final Tag<?> tag;

    public IllegalEntityTagException(Tag<?> tag) {
        super();
        this.tag = tag;
    }

    public IllegalEntityTagException(Tag<?> tag, String message) {
        super(message);
        this.tag = tag;
    }

    public IllegalEntityTagException(Tag<?> tag, String message, Throwable cause) {
        super(message, cause);
        this.tag = tag;
    }

    public IllegalEntityTagException(Tag<?> tag, Throwable cause) {
        super(cause);
        this.tag = tag;
    }

    /** Gets the tag which caused this exception to be raised. */
    public Tag<?> getTag() {
        return tag;
    }
}
