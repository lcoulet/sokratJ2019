package com.kratos.sokratj.algorithm;

import com.kratos.sokratj.SolutionSerializer;
import com.kratos.sokratj.model.SlideOptiB;
import com.kratos.sokratj.utils.Score;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class RecuitOnlyB {
    private List<SlideOptiB> startSolution;
    private Random random;

    private List<SlideOptiB> best;
    private long bestScore;

    private final String name;

    public RecuitOnlyB(final List<SlideOptiB> photoList, String name) {
        random = new Random(System.currentTimeMillis());
        startSolution = photoList;
        System.out.println(Score.getScoreOptiB(startSolution));
        this.name = name;
    }

    public List<SlideOptiB> optimize(final String filename) throws FileNotFoundException {
        SolutionSerializer serializer = new SolutionSerializer();
        double startTemperature = 1;
        double temperature = startTemperature;
        double tau = 5000000;
        double temperatureLimit = 0.001;

        long referenceScore = Score.getScoreOptiB(startSolution);
        long i = 0;

        long lastCheckScore = referenceScore;

        bestScore = referenceScore;
        best = startSolution;

        while (true) {
            List<SlideOptiB> newSolution = new ArrayList<>(startSolution);
            int firstSlide;
            int secondSlide;

            firstSlide = random.nextInt(startSolution.size());
            secondSlide = random.nextInt(startSolution.size());
            while (secondSlide == firstSlide) {
                secondSlide = random.nextInt(startSolution.size());
            }
            Collections.swap(newSolution, firstSlide, secondSlide);

            long newScore = referenceScore - getDeltaScore(startSolution, newSolution, firstSlide, secondSlide);
            if (newScore > referenceScore
                    || Math.exp(-(referenceScore - newScore) / temperature) > random.nextDouble()) {
                startSolution = newSolution;
                referenceScore = newScore;
            }

            if (bestScore < referenceScore) {
                bestScore = referenceScore;
                best = startSolution;
            }

            temperature = startTemperature * Math.exp(-i / tau);

            ++i;
            if (i % 400000 == 0) {
                System.out.println(name + " " + temperature + " " + bestScore + " (" + i + ")");
                serializer.serializeSolutionToFileOptiB(best, new File(filename));
                if (lastCheckScore == bestScore && temperature < temperatureLimit) {
                    break;
                }
                lastCheckScore = bestScore;
            }
        }
        System.out.println(name + " " + bestScore + " " + Score.getScoreOptiB(best) + "(" + i + ")");
        return best;
    }

    private long getDeltaScore(final List<SlideOptiB> list, final List<SlideOptiB> newList, final int slide1, final int slide2) {
        if (slide1 == slide2) {
            return 0;
        }
        else if (Math.abs(slide1 - slide2) == 1) {
            return handleConsecutive(list, newList, slide1, slide2);
        }
        int firstSlide = Math.min(slide1, slide2);
        int secondSlide = Math.max(slide1, slide2);
        long previousScore;
        long newScore;


        if (firstSlide == 0) {
            if (secondSlide == list.size() - 1) {
                previousScore = Score.computeScore(list.get(firstSlide), list.get(firstSlide + 1))
                        + Score.computeScore(list.get(secondSlide - 1), list.get(secondSlide));
                newScore =Score.computeScore(newList.get(firstSlide), newList.get(firstSlide + 1))
                        + Score.computeScore(newList.get(secondSlide - 1), newList.get(secondSlide));
            }
            else {
                previousScore = Score.computeScore(list.get(firstSlide), list.get(firstSlide + 1))
                        + Score.computeScore(list.get(secondSlide - 1), list.get(secondSlide))
                        + Score.computeScore(list.get(secondSlide), list.get(secondSlide + 1));
                newScore = Score.computeScore(newList.get(firstSlide), newList.get(firstSlide + 1))
                        + Score.computeScore(newList.get(secondSlide - 1), newList.get(secondSlide))
                        + Score.computeScore(newList.get(secondSlide), newList.get(secondSlide + 1));
            }
        }
        else if (secondSlide == list.size() - 1) {
            previousScore = Score.computeScore(list.get(firstSlide - 1), list.get(firstSlide))
                    + Score.computeScore(list.get(firstSlide), list.get(firstSlide + 1))
                    + Score.computeScore(list.get(secondSlide - 1), list.get(secondSlide));
            newScore = Score.computeScore(newList.get(firstSlide - 1), newList.get(firstSlide))
                    + Score.computeScore(newList.get(firstSlide), newList.get(firstSlide + 1))
                    + Score.computeScore(newList.get(secondSlide - 1), newList.get(secondSlide));
        }
        else {
            previousScore = Score.computeScore(list.get(firstSlide - 1), list.get(firstSlide))
                    + Score.computeScore(list.get(firstSlide), list.get(firstSlide + 1))
                    + Score.computeScore(list.get(secondSlide - 1), list.get(secondSlide))
                    + Score.computeScore(list.get(secondSlide), list.get(secondSlide + 1));
            newScore = Score.computeScore(newList.get(firstSlide - 1), newList.get(firstSlide))
                    + Score.computeScore(newList.get(firstSlide), newList.get(firstSlide + 1))
                    + Score.computeScore(newList.get(secondSlide - 1), newList.get(secondSlide))
                    + Score.computeScore(newList.get(secondSlide), newList.get(secondSlide + 1));
        }
        //System.out.println(firstSlide + " " +secondSlide + " " + newScore + " " + previousScore);
        return previousScore - newScore;
    }

    private long handleConsecutive(final List<SlideOptiB> list, final List<SlideOptiB> newList, final int slide1, final int slide2) {
        int firstSlide = Math.min(slide1, slide2);
        int secondSlide = Math.max(slide1, slide2);
        long previousScore;
        long newScore;

        if (firstSlide == 0) {
            previousScore = Score.computeScore(list.get(firstSlide), list.get(secondSlide))
                    + Score.computeScore(list.get(secondSlide), list.get(secondSlide + 1));
            newScore = Score.computeScore(newList.get(firstSlide), newList.get(secondSlide))
                    + Score.computeScore(newList.get(secondSlide), newList.get(secondSlide + 1));
        }
        else if (secondSlide == list.size() - 1) {
            previousScore = Score.computeScore(list.get(firstSlide - 1), list.get(firstSlide))
                    + Score.computeScore(list.get(firstSlide), list.get(secondSlide));
            newScore = Score.computeScore(newList.get(firstSlide - 1), newList.get(firstSlide))
                    + Score.computeScore(newList.get(firstSlide), newList.get(secondSlide));
        }
        else {
            previousScore = Score.computeScore(list.get(firstSlide - 1), list.get(firstSlide))
                    + Score.computeScore(list.get(firstSlide), list.get(secondSlide))
                    + Score.computeScore(list.get(secondSlide), list.get(secondSlide + 1));
            newScore = Score.computeScore(newList.get(firstSlide - 1), newList.get(firstSlide))
                    + Score.computeScore(newList.get(firstSlide), newList.get(secondSlide))
                    + Score.computeScore(newList.get(secondSlide), newList.get(secondSlide + 1));
        }
        return previousScore - newScore;
    }
}
