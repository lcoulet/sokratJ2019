package com.kratos.sokratj.model;

import java.util.List;

public class SlideMutable extends Slide {
    private final List<Photo> photoList;

    public SlideMutable(final List<Photo> photoList) {
        this.photoList = photoList;
        id = super.hashCode();
    }

    public SlideMutable(final List<Photo> photoList, final int id) {
        this.photoList = photoList;
        this.id = id;
    }

    @Override
    public List<Photo> getPhotos() {
        return photoList;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof SlideMutable) {
            return this.id == ((SlideMutable) obj).id;
        }
        return false;
    }
}
