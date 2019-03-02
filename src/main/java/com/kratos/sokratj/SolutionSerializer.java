package com.kratos.sokratj;

import com.google.common.base.Joiner;
import com.kratos.sokratj.model.Slide;
import com.kratos.sokratj.model.SlideOpti;
import com.kratos.sokratj.model.SlideOptiB;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SolutionSerializer
 *
 * @author Loic.Coulet
 */
public class SolutionSerializer {

    public String serializeSolution(List<Slide> solution){

        return solution.size()
                + "\n"
                + Joiner.on("\n").join(
                solution.stream().map(
                        slide -> Joiner.on(" ").join(
                                slide.getPhotos().stream().map(photo -> String.valueOf(photo.getId())).collect(Collectors.toList())
                        )
                ).collect(Collectors.toList())
        );
    }

    public void serializeSolutionToFile( List<Slide> solution , File outputFile) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(outputFile);
        writer.println(serializeSolution(solution));
        writer.close();
    }

    public String serializeSolutionOpti(List<SlideOpti> solution){

        return solution.size()
                + "\n"
                + Joiner.on("\n").join(
                solution.stream().map(
                        slide -> Joiner.on(" ").join(
                                slide.getPhotos().stream().map(photo -> String.valueOf(photo.getId())).collect(Collectors.toList())
                        )
                ).collect(Collectors.toList())
        );
    }


    public void serializeSolutionToFileOpti(List<SlideOpti> solution , File outputFile) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(outputFile);
        writer.println(serializeSolutionOpti(solution));
        writer.close();
    }


    public String serializeSolutionOptiB(List<SlideOptiB> solution){

        return solution.size()
                + "\n"
                + Joiner.on("\n").join(
                solution.stream().map(
                        slide -> Joiner.on(" ").join(
                                Collections.singletonList(slide.getPhoto().getId())
                        )
                ).collect(Collectors.toList())
        );
    }

    public void serializeSolutionToFileOptiB(List<SlideOptiB> solution , File outputFile) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(outputFile);
        writer.println(serializeSolutionOptiB(solution));
        writer.close();
    }

}
