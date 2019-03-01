package com.kratos.sokratj.model;

import org.immutables.value.Value;

import java.util.List;

/**
 * Slide
 *
 * @author Loic.Coulet
 */
@Value.Immutable
public abstract class Slide {

    public abstract List<Photo> getPhotos();

    int id;
    public int getId() {
        return id;
    }

}
