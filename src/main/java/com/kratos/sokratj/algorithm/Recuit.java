package com.kratos.sokratj.algorithm;

import com.kratos.sokratj.SolutionSerializer;
import com.kratos.sokratj.model.ImmutableSlide;
import com.kratos.sokratj.model.Photo;
import com.kratos.sokratj.model.Slide;
import com.kratos.sokratj.model.SlideMutable;
import com.kratos.sokratj.parser.PhotoParser;
import com.kratos.sokratj.utils.Score;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Recuit {
    private List<Slide> startSolution;

    public Recuit(final List<Photo> photoList) {
        startSolution = generateSolution(photoList);
        System.out.println(Score.getScore(startSolution));
    }

    public List<Slide> optimize() {
        return startSolution;
    }

    private List<Slide> generateSolution(final List<Photo> photoList) {
        Photo verticalBuffer = null;

        List<Slide> solutionList = new ArrayList<>();

        for (Photo photo : photoList) {
            if(photo.isVertical()) {
                if (verticalBuffer == null) {
                    verticalBuffer = photo;
                }
                else {
                    solutionList.add(new SlideMutable(Arrays.asList(verticalBuffer, photo)));
                    verticalBuffer = null;
                }
            }
            else {
                solutionList.add(new SlideMutable(Arrays.asList(photo)));
            }
        }
        return solutionList;
    }


    public static void main(final String[] args) throws IOException {
        List<String> fileList = Arrays.asList("data/b_lovely_landscapes.txt",
                                              "data/c_memorable_moments.txt",
                                              "data/d_pet_pictures.txt",
                                              "data/e_shiny_selfies.txt");
        List<String> res = Arrays.asList("recuit_b.text",
                                         "recuit_c.text",
                                         "recuit_d.text",
                                         "recuit_e.text");
        for (int i = 0; i < fileList.size(); i++) {
            String s = fileList.get(i);
            List<Photo> photos = new PhotoParser().parseData(s);
            Recuit recuit = new Recuit(photos);
            SolutionSerializer serializer = new SolutionSerializer();
            serializer.serializeSolutionToFile(recuit.optimize(), new File(res.get(i)));
        }
    }
}
