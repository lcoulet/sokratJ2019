package com.kratos.sokratj.model;

import org.immutables.value.internal.$processor$.meta.$ValueMirrors;

import java.util.List;

/**
 * SlideShow
 *
 * @author Loic.Coulet
 */
@$ValueMirrors.Immutable
public interface SlideShow {
    public List<Slide> getSlides();
}
