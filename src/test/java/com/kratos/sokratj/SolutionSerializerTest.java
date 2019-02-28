package com.kratos.sokratj;

import com.kratos.sokratj.model.*;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class SolutionSerializerTest {
    Photo[] photos = new Photo[]{
      ImmutablePhoto.builder().id(0).addTags("toto","tata").isVertical(false).build(),
      ImmutablePhoto.builder().id(1).addTags("a","w").isVertical(false).build(),
      ImmutablePhoto.builder().id(2).addTags("b","x","g","j").isVertical(true).build(),
      ImmutablePhoto.builder().id(3).addTags("c","y").isVertical(false).build(),
      ImmutablePhoto.builder().id(4).addTags("d","z","v").isVertical(true).build(),
    };

    @Test
    public void testSerialize() {
        List<Slide> data = createSomeSlides();

        System.out.println(new SolutionSerializer().serializeSolution(data));
    }


    @Test
    public void testSerializetoFile() throws FileNotFoundException {
        List<Slide> data = createSomeSlides();

        new SolutionSerializer().serializeSolutionToFile(createSomeSlides(), new File("testresults.txt"));
    }

    private List<Slide> createSomeSlides() {
        List<Slide> data = new ArrayList<>();
        data.add(ImmutableSlide.builder().addPhotos(photos[0], photos[1]).build());
        data.add(ImmutableSlide.builder().addPhotos(photos[2], photos[3], photos[1]).build());
        data.add(ImmutableSlide.builder().addPhotos(photos[4]).build());
        return data;
    }


}