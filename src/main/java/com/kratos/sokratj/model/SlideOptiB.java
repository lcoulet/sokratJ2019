package com.kratos.sokratj.model;

import java.util.List;

public class SlideOptiB {
    private final PhotoOpti photo;
    private final int id;
    private final int unique;

    public SlideOptiB(final PhotoOpti photo, final int id, final int unique) {
        this.photo = photo;
        this.id = id;
        this.unique = unique;
    }

    public PhotoOpti getPhoto() {
        return photo;
    }

    public int getId() {
        return id;
    }

    public int getUnique() {
        return unique;
    }

    public int hashCode() {
        return id;
    }

    public boolean equals(final Object obj) {
        if (obj instanceof SlideOptiB) {
            return this.id == ((SlideOptiB) obj).id;
        }
        return false;
    }
}
