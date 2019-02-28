package com.kratos.sokratj.model;

import org.immutables.value.Value;

import java.util.List;

/**
 * Photo
 *
 * @author Loic.Coulet
 */
@Value.Immutable
public interface Photo {

    List<String> getTags();
    int getId();
    boolean isVertical();

}
