package com.kratos.sokratj;

import com.kratos.sokratj.model.ImmutableSlide;
import com.kratos.sokratj.model.Photo;
import com.kratos.sokratj.model.Slide;
import com.kratos.sokratj.parser.PhotoParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(final String[] args) throws IOException {
        List<Photo> photos1 = new PhotoParser().parseData("data/a_example.txt");

        new SolutionSerializer().serializeSolutionToFile(createSlideShowForPhotosSet1(photos1), new File("a_example.results"));

    }

    private static List<Slide> createSlideShowForPhotosSet1(List<Photo> photos1) {
        List<Slide> data = new ArrayList<>();
        data.add(ImmutableSlide.builder().addPhotos(photos1.get(0)).build());
        data.add(ImmutableSlide.builder().addPhotos(photos1.get(3)).build());
        data.add(ImmutableSlide.builder().addPhotos(photos1.get(1),photos1.get(2)).build());
        return data;
    }
}
