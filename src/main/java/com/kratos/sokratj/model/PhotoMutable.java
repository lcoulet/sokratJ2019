package com.kratos.sokratj.model;

import java.util.List;

public class PhotoMutable implements Photo {
    private final List<String> tags;
    private final int id;
    private final boolean vertical;

    public PhotoMutable(final List<String> tags, final int id, final boolean vertical) {
        this.tags = tags;
        this.id = id;
        this.vertical = vertical;
    }

    @Override
    public List<String> getTags() {
        return null;
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public boolean isVertical() {
        return false;
    }
}
