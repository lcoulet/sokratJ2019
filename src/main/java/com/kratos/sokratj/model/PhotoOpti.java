package com.kratos.sokratj.model;

import java.util.List;

public class PhotoOpti {
    private final List<Long> tags;
    private final int id;
    private final boolean vertical;
    private final int unique;

    public PhotoOpti(final List<Long> tags,
                     final int id,
                     final boolean vertical,
                     final int unique) {
        this.tags = tags;
        this.id = id;
        this.vertical = vertical;
        this.unique = unique;
    }

    public List<Long> getTags() {
        return tags;
    }

    public int getId() {
        return id;
    }

    public boolean isVertical() {
        return vertical;
    }

    public int getUnique() {
        return unique;
    }
}
