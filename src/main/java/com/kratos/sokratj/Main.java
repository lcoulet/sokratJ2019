package com.kratos.sokratj;

import com.kratos.sokratj.algorithm.Stupid;
import com.kratos.sokratj.model.ImmutableSlide;
import com.kratos.sokratj.model.Photo;
import com.kratos.sokratj.model.Slide;
import com.kratos.sokratj.parser.PhotoParser;
import com.kratos.sokratj.utils.Score;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(final String[] args) throws IOException {
        List<Photo> photos1 = new PhotoParser().parseData("data/a_example.txt");
        new SolutionSerializer().serializeSolutionToFile(createSlideShowForPhotosSet1(photos1), new File("a_example.results"));

        doDataSet("a_example");
        doDataSet("b_lovely_landscapes");
        doDataSet("c_memorable_moments");
        doDataSet("d_pet_pictures");
        doDataSet("e_shiny_selfies");

    }

    private static void doDataSet(String dataSet) throws IOException {
        List<Photo> photos = new PhotoParser().parseData("data/"+dataSet+".txt");
        List<Slide> solution = new Stupid().compute(photos);
        System.out.println("SCORE " + dataSet + " : "+ Score.getScore(solution));
        new SolutionSerializer().serializeSolutionToFile(solution, new File(dataSet+".results"));
    }

    private static List<Slide> createSlideShowForPhotosSet1(List<Photo> photos1) {
        List<Slide> data = new ArrayList<>();
        data.add(ImmutableSlide.builder().addPhotos(photos1.get(0)).build());
        data.add(ImmutableSlide.builder().addPhotos(photos1.get(3)).build());
        data.add(ImmutableSlide.builder().addPhotos(photos1.get(1),photos1.get(2)).build());
        return data;
    }
}
