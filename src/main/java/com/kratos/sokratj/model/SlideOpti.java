package com.kratos.sokratj.model;

import java.util.List;
import java.util.stream.Collectors;

public class SlideOpti {
    private final List<PhotoOpti> photoList;
    private final int id;
    private final boolean isDouble;
    private List<Long> tagList;

    public SlideOpti(final List<PhotoOpti> photoList, final int id) {
        this.photoList = photoList;
        this.id = id;
        isDouble = photoList.size() != 1;
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

    public boolean isDouble() {
        return isDouble;
    }

    public void generateTagList() {
        tagList = getPhotos().stream()
                             .flatMap(photo -> photo.getTags().stream())
                             .distinct()
                             .collect(Collectors.toList());
    }

    public List<Long> getTagList() {
        return tagList;
    }

    public boolean equals(final Object obj) {
        if (obj instanceof SlideOpti) {
            return this.id == ((SlideOpti) obj).id;
        }
        return false;
    }
}
