package com.kratos.sokratj.model;

import java.util.List;

public class SlideMutable implements Slide {
    private final List<Photo> photoList;

    public SlideMutable(final List<Photo> photoList) {
        this.photoList = photoList;
    }

    @Override
    public List<Photo> getPhotos() {
        return null;
    }
}
