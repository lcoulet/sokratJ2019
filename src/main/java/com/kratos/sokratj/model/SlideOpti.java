package com.kratos.sokratj.model;

import java.util.List;

public class SlideOpti {
    private final List<PhotoOpti> photoList;
    private final int id;

    public SlideOpti(final List<PhotoOpti> photoList, final int id) {
        this.photoList = photoList;
        this.id = id;
    }

    public List<PhotoOpti> getPhotos() {
        return photoList;
    }

    public int getId() {
        return id;
    }

    public int hashCode() {
        return id;
    }

    public boolean equals(final Object obj) {
        if (obj instanceof SlideOpti) {
            return this.id == ((SlideOpti) obj).id;
        }
        return false;
    }
}
