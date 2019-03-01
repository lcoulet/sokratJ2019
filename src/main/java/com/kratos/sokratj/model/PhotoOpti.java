package com.kratos.sokratj.model;

import java.util.List;

public class PhotoOpti {
    private final List<Long> tags;
    private final int id;
    private final boolean vertical;

    public PhotoOpti(final List<Long> tags, final int id, final boolean vertical) {
        this.tags = tags;
        this.id = id;
        this.vertical = vertical;
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
}
