package com.kratos.sokratj;

import com.google.common.base.Stopwatch;
import com.kratos.sokratj.algorithm.Stupid;
import com.kratos.sokratj.model.ImmutableSlide;
import com.kratos.sokratj.model.Photo;
import com.kratos.sokratj.model.Slide;
import com.kratos.sokratj.parser.PhotoParser;
import com.kratos.sokratj.utils.Score;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Main {

    public static final String A_EXAMPLE = "a_example";
    public static final String B_LOVELY_LANDSCAPES = "b_lovely_landscapes";
    public static final String C_MEMORABLE_MOMENTS = "c_memorable_moments";
    public static final String D_PET_PICTURES = "d_pet_pictures";
    public static final String E_SHINY_SELFIES = "e_shiny_selfies";

    static Map<String,Long> scores=new HashMap<>();
    static Map<String,Long> maxScores=new HashMap<>();

    static StringBuilder scoresStr = new StringBuilder();

    public static void main(final String[] args) throws IOException {
        List<Photo> photos1 = new PhotoParser().parseData("data/a_example.txt");
        new SolutionSerializer().serializeSolutionToFile(createSlideShowForPhotosSet1(photos1), new File("a_example.results"));


        doDataSetAndRecordScore(A_EXAMPLE);
        doDataSetAndRecordScore(B_LOVELY_LANDSCAPES);
        doDataSetAndRecordScore(C_MEMORABLE_MOMENTS);
        doDataSetAndRecordScore(D_PET_PICTURES);
        doDataSetAndRecordScore(E_SHINY_SELFIES);

        scores.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey)).forEach(stringLongEntry -> System.out.println("SCORE "  + stringLongEntry.getKey() + ": " + stringLongEntry.getValue()));
        String totalMsg = "========= TOTAL : " + scores.values().stream().mapToLong(o -> o).sum() + "(MAX? : " + maxScores.values().stream().mapToLong(o -> o).sum() + ")\n";
        System.out.println(totalMsg);
        scoresStr.append(totalMsg);

        PrintWriter printWriter = new PrintWriter("scores.txt");
        printWriter.println(scoresStr.toString());
        printWriter.close();

    }

    private static void doDataSetAndRecordScore(String dataSet) throws IOException {
        scores.put(dataSet, doDataSet(dataSet));
    }

    private static long doDataSet(String dataSet) throws IOException {

        Stopwatch timer = Stopwatch.createStarted();
        List<Photo> photos = new PhotoParser().parseData("data/"+dataSet+".txt");
        long maximalTheoricalScore = Score.maximalTheoricalScore(photos);
        System.out.println("max SCORE of " + dataSet + " : " + maximalTheoricalScore + "?");
        maxScores.put(dataSet, maximalTheoricalScore);
        List<Slide> solution = new Stupid().compute(photos);
        long score = Score.getScore(solution);
        String scoreMsg = "SCORE of " + dataSet + " : " + score + " (max " + maximalTheoricalScore + "?)\n";
        System.out.println(scoreMsg);
        scoresStr.append(scoreMsg);
        new SolutionSerializer().serializeSolutionToFile(solution, new File(dataSet+".results"));
        System.out.println("TIME:  " + timer.elapsed(TimeUnit.SECONDS) + " sec.");
        Score.checkResults(solution);
        return score;
    }

    private static List<Slide> createSlideShowForPhotosSet1(List<Photo> photos1) {
        List<Slide> data = new ArrayList<>();
        data.add(ImmutableSlide.builder().addPhotos(photos1.get(0)).build());
        data.add(ImmutableSlide.builder().addPhotos(photos1.get(3)).build());
        data.add(ImmutableSlide.builder().addPhotos(photos1.get(1),photos1.get(2)).build());
        return data;
    }
}
