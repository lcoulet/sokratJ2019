package com.kratos.sokratj.algorithm;

import com.kratos.sokratj.SolutionSerializer;
import com.kratos.sokratj.model.*;
import com.kratos.sokratj.parser.PhotoParser;
import com.kratos.sokratj.utils.Score;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

public class RecuitMixedOnlyB {
    private List<SlideOptiB> startSolution;
    private Random random;

    private List<SlideOptiB> best;
    private long bestScore;

    private final String name;
    private final int solutionSize;

    public RecuitMixedOnlyB(final List<SlideOptiB> slideList, String name) {
        random = new Random(System.currentTimeMillis());
        startSolution = slideList;
        solutionSize = startSolution.size();

        System.out.println(Score.getScoreOptiB(startSolution));
        this.name = name;
    }

    public List<SlideOptiB> optimize(final String filename) throws FileNotFoundException {
        SolutionSerializer serializer = new SolutionSerializer();
        double startTemperature = 0.1;
        double temperature = startTemperature;
        double tau = 5000000;
        double temperatureLimit = 0.001;


        long referenceScore = Score.getScoreOptiB(startSolution);
        long i = 0;

        long lastCheckScore = referenceScore;

        bestScore = referenceScore;
        best = startSolution;

        Instant ref = Instant.now();
        while (true) {
            List<SlideOptiB> newSolution;
            int firstSlide;
            int secondSlide;
            long newScore;

            int choice = random.nextInt(3);
            if (choice == 0) {
                newSolution = new ArrayList<>(startSolution);
                firstSlide = random.nextInt(solutionSize);
                secondSlide = random.nextInt(solutionSize);
                while (secondSlide == firstSlide) {
                    secondSlide = random.nextInt(solutionSize);
                }
                Collections.swap(newSolution, firstSlide, secondSlide);
                newScore = referenceScore - getDeltaScore(startSolution, newSolution, firstSlide, secondSlide);
            }
            else if (choice == 1) {
                firstSlide = random.nextInt(solutionSize);
                secondSlide = random.nextInt(solutionSize);
                while (Math.abs(secondSlide - firstSlide) < 2) { //we don't want consecutive
                    secondSlide = random.nextInt(solutionSize);
                }
                int little = Math.min(firstSlide, secondSlide);
                int big = Math.max(firstSlide, secondSlide);

                newSolution = new ArrayList<>(startSolution.subList(0, little));
                List<SlideOptiB> toReverse = startSolution.subList(little, big + 1);
                for (int k = toReverse.size() - 1; k >=0; --k ) {
                    newSolution.add(toReverse.get(k));
                }

                newSolution.addAll(startSolution.subList(big + 1, solutionSize));
                newScore = referenceScore - getReverseScore(startSolution, newSolution, little, big);
            }
            else {
                firstSlide = random.nextInt(solutionSize);
                secondSlide = random.nextInt(solutionSize);
                while (Math.abs(secondSlide - firstSlide) < 2) { //we don't want consecutive
                    secondSlide = random.nextInt(solutionSize);
                }

                newSolution = new ArrayList<>(startSolution);
                SlideOptiB switched = newSolution.remove(firstSlide);
                newSolution.add(secondSlide, switched);
                newScore = referenceScore - getInsertScore(startSolution, newSolution, firstSlide, secondSlide);
            }

            if (newScore >= referenceScore) {
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
                Instant newRef = Instant.now();
                System.out.println(name + " " + temperature + " " + bestScore + " (" + i + " : " + (newRef.toEpochMilli() - ref.toEpochMilli()) +")");
                ref = newRef;
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
//        System.out.println(firstSlide + " " +secondSlide + " " + newScore + " " + previousScore);
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

    private long getReverseScore(final List<SlideOptiB> list, final List<SlideOptiB> newList, final int slide1, final int slide2) {
        long previousScore = 0;
        long newScore = 0;

        if (slide1 != 0) {
            previousScore += Score.computeScore(list.get(slide1 - 1), list.get(slide1));
            newScore += Score.computeScore(newList.get(slide1 - 1), newList.get(slide1));
        }
        if (slide2 != solutionSize - 1) {
            previousScore += Score.computeScore(list.get(slide2), list.get(slide2 + 1));
            newScore += Score.computeScore(newList.get(slide2), newList.get(slide2 + 1));
        }

        return previousScore - newScore;
    }

    private long getInsertScore(final List<SlideOptiB> list, final List<SlideOptiB> newList, final int slide1, final int slide2) {
        if (slide1 < slide2) {
            return getInsertScoreLower(list, newList, slide1, slide2);
        }
        else {
            return getInsertScoreHigher(list, newList, slide1, slide2);
        }
    }

    private long getInsertScoreLower(final List<SlideOptiB> list, final List<SlideOptiB> newList, final int slide1, final int slide2) {
        long previousScore = Score.computeScore(list.get(slide1 + 1), list.get(slide1));
        long newScore = Score.computeScore(newList.get(slide2 - 1), newList.get(slide2));

        if (slide1 != 0) {
            previousScore += Score.computeScore(list.get(slide1 - 1), list.get(slide1));
            newScore += Score.computeScore(newList.get(slide1 - 1), newList.get(slide1));
        }

        if (slide2 != solutionSize - 1) {
            previousScore += Score.computeScore(list.get(slide2 + 1), list.get(slide2));
            newScore += Score.computeScore(newList.get(slide2), newList.get(slide2 + 1));
        }
//        System.out.println("lower " + slide1 + " " +slide2 + " " + newScore + " " + previousScore);
        return previousScore - newScore;
    }

    private long getInsertScoreHigher(final List<SlideOptiB> list, final List<SlideOptiB> newList, final int slide1, final int slide2) {
        long previousScore = Score.computeScore(list.get(slide1 - 1), list.get(slide1));
        long newScore = Score.computeScore(newList.get(slide2 + 1), newList.get(slide2));

        if (slide2 != 0) {
            previousScore += Score.computeScore(list.get(slide2 - 1), list.get(slide2));
            newScore += Score.computeScore(newList.get(slide2 - 1), newList.get(slide2));
        }

        if (slide1 != solutionSize - 1) {
            previousScore += Score.computeScore(list.get(slide1 + 1), list.get(slide1));
            newScore += Score.computeScore(newList.get(slide1 + 1), newList.get(slide1));
        }
//        System.out.println("higher " + slide1 + " " +slide2 + " " + newScore + " " + previousScore);
        return previousScore - newScore;
    }


    private List<SlideOpti> generateSolution(final List<Photo> photoList) {
        List<PhotoOpti> optiList = PhotoParser.optimize(photoList);
        PhotoOpti verticalBuffer = null;

        List<SlideOpti> solutionList = new ArrayList<>();

        int currentIndex = 0;

        for (PhotoOpti photo : optiList) {
            if (photo.isVertical()) {
                if (verticalBuffer == null) {
                    verticalBuffer = photo;
                }
                else {
                    SlideOpti slide = new SlideOpti(Arrays.asList(verticalBuffer, photo), currentIndex);
                    solutionList.add(slide);
                    verticalBuffer = null;
                    ++currentIndex;
                }
            }
            else {
                solutionList.add(new SlideOpti(Arrays.asList(photo), currentIndex));
                ++currentIndex;
            }
        }
        return solutionList;
    }
}
