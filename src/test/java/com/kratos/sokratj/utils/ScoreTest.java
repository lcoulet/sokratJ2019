package com.kratos.sokratj.utils;

import com.kratos.sokratj.model.ImmutablePhoto;
import com.kratos.sokratj.model.ImmutableSlide;
import com.kratos.sokratj.model.Photo;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class ScoreTest {
    private static ImmutableSlide getSlide(final Photo... photos) {
        return ImmutableSlide.builder().addAllPhotos(Arrays.asList(photos)).build();
    }

    @Test
    public void testScore() {
        ImmutablePhoto photo1 = ImmutablePhoto.builder().addAllTags(Arrays.asList("tag1", "tag2", "tag3"))
                                              .id(1).isVertical(false).build();
        ImmutablePhoto photo2 = ImmutablePhoto.builder().addAllTags(Arrays.asList("tag2", "tag3", "tag4"))
                                              .id(1).isVertical(false).build();
        ImmutablePhoto photo3 = ImmutablePhoto.builder().addAllTags(Arrays.asList("tag5"))
                                              .id(1).isVertical(false).build();
        ImmutablePhoto photo4 = ImmutablePhoto.builder().addAllTags(Arrays.asList("tag6", "tag3", "tag4"))
                                              .id(1).isVertical(false).build();

        assertEquals(1, Score.computeScore(getSlide(photo1), getSlide(photo2)));
        assertEquals(0, Score.computeScore(getSlide(photo1), getSlide(photo3)));
        assertEquals(2, Score.computeScore(getSlide(photo1, photo3), getSlide(photo2, photo4)));
    }
}