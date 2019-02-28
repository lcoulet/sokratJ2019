package com.kratos.sokratj.algorithm;

import com.kratos.sokratj.model.ImmutableSlide;
import com.kratos.sokratj.model.Photo;
import com.kratos.sokratj.utils.Score;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Recuit {
    private static ImmutableSlide getSlide(final Photo... photos) {
        return ImmutableSlide.builder().addAllPhotos(Arrays.asList(photos)).build();
    }

    public Recuit(final List<Photo> photoList) {
        List<ImmutableSlide> solution = generateSolution(photoList);
        System.out.println(Score.getScore(solution));
    }

    private List<ImmutableSlide> generateSolution(final List<Photo> photoList) {
        Photo verticalBuffer = null;

        List<ImmutableSlide> solutionList = new ArrayList<>();

        for (Photo photo : photoList) {
            if(photo.isVertical()) {
                if (verticalBuffer == null) {
                    verticalBuffer = photo;
                }
                else {
                    solutionList.add(getSlide(verticalBuffer, photo));
                    verticalBuffer = null;
                }
            }
            else {
                solutionList.add(getSlide(photo));
            }
        }
        return solutionList;
    }


    public static void main(final String[] args) {
        
    }
}
