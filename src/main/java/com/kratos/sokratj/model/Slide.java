package com.kratos.sokratj.model;

import org.immutables.value.Value;

import java.util.List;

/**
 * Slide
 *
 * @author Loic.Coulet
 */
@Value.Immutable
public interface Slide {

    List<Photo> getPhotos();

}
