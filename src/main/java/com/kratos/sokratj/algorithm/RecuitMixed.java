package com.kratos.sokratj.algorithm;

import com.kratos.sokratj.SolutionSerializer;
import com.kratos.sokratj.model.Photo;
import com.kratos.sokratj.model.PhotoOpti;
import com.kratos.sokratj.model.Slide;
import com.kratos.sokratj.model.SlideOpti;
import com.kratos.sokratj.parser.PhotoParser;
import com.kratos.sokratj.utils.Score;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

public class RecuitMixed {
    private List<SlideOpti> startSolution;
    private Random random;

    private List<SlideOpti> best;
    private long bestScore;

    private final String name;
    private final int solutionSize;

    public RecuitMixed(final List<Photo> photoList, String name) {
        random = new Random(System.currentTimeMillis());
        startSolution = generateSolution(photoList);
        solutionSize = startSolution.size();

        System.out.println(Score.getScoreOpti(startSolution));
        this.name = name;
    }

    public List<SlideOpti> optimize(final String filename) throws FileNotFoundException {
        SolutionSerializer serializer = new SolutionSerializer();
        double startTemperature = 1;
        double temperature = startTemperature;
        double tau = 5000000;
        double temperatureLimit = 0.001;


        long referenceScore = Score.getScoreOpti(startSolution);
        long i = 0;

        long lastCheckScore = referenceScore;

        bestScore = referenceScore;
        best = startSolution;

        Instant ref = Instant.now();
        while (true) {
            List<SlideOpti> newSolution;
            int firstSlide;
            int secondSlide;
            long newScore;

            int choice = random.nextInt(4);
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
                List<SlideOpti> toReverse = startSolution.subList(little, big + 1);
                for (int k = toReverse.size() - 1; k >=0; --k ) {
                    newSolution.add(toReverse.get(k));
                }

                newSolution.addAll(startSolution.subList(big + 1, solutionSize));
                newScore = referenceScore - getReverseScore(startSolution, newSolution, little, big);
            }
            else if (choice == 3) {
                firstSlide = random.nextInt(solutionSize);
                secondSlide = random.nextInt(solutionSize);
                while (Math.abs(secondSlide - firstSlide) < 2) { //we don't want consecutive
                    secondSlide = random.nextInt(solutionSize);
                }

                newSolution = new ArrayList<>(startSolution);
                SlideOpti switched = newSolution.remove(firstSlide);
                newSolution.add(secondSlide, switched);
                newScore = referenceScore - getInsertScore(startSolution, newSolution, firstSlide, secondSlide);
//                System.out.println(startSolution.get(firstSlide - 1).getId() + " " + startSolution.get(firstSlide).getId() + " " +startSolution.get(firstSlide + 1).getId() + " ... " + startSolution.get(secondSlide - 1).getId() + " " + startSolution.get(secondSlide).getId() + " " + startSolution.get(secondSlide + 1).getId());
//                System.out.println(newSolution.get(firstSlide - 1).getId() + " " + newSolution.get(firstSlide).getId() + " " +newSolution.get(firstSlide + 1).getId() + " ... " + newSolution.get(secondSlide - 1).getId() + " " +newSolution.get(secondSlide).getId() + " " + newSolution.get(secondSlide + 1).getId());
//                System.out.println(newScore + " " + Score.getScoreOpti(newSolution));
            }
            else {
                newSolution = new ArrayList<>(startSolution);
                firstSlide = getDoubleSlide(-1);
                secondSlide = getDoubleSlide(firstSlide);

                int firstOne = random.nextInt(2);
                int secondOne = random.nextInt(2);
                SlideOpti slide1 = new SlideOpti(new ArrayList<>(startSolution.get(firstSlide).getPhotos()),
                                                 startSolution.get(firstSlide).getId());
                SlideOpti slide2 = new SlideOpti(new ArrayList<>(startSolution.get(secondSlide).getPhotos()),
                                                 startSolution.get(secondSlide).getId());

                PhotoOpti temp = startSolution.get(firstSlide).getPhotos().get(firstOne);
                PhotoOpti temp2 = startSolution.get(secondSlide).getPhotos().get(secondOne);
                slide1.getPhotos().set(firstOne, temp2);
                slide2.getPhotos().set(secondOne, temp);

                newSolution.set(firstSlide, slide1);
                newSolution.set(secondSlide, slide2);
                newScore = referenceScore - getDeltaScore(startSolution, newSolution, firstSlide, secondSlide);
            }

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
                Instant newRef = Instant.now();
                System.out.println(name + " " + temperature + " " + bestScore + " (" + i + " : " + (newRef.toEpochMilli() - ref.toEpochMilli()) +")");
                ref = newRef;
                serializer.serializeSolutionToFileOpti(best, new File(filename));
                if (lastCheckScore == bestScore && temperature < temperatureLimit) {
                    break;
                }
                lastCheckScore = bestScore;
            }
        }
        System.out.println(name + " " + bestScore + " " + Score.getScoreOpti(best) + "(" + i + ")");
        return best;
    }

    private int getDoubleSlide(final int avoided) {
        int res = random.nextInt(solutionSize);
        while (!startSolution.get(res).isDouble() || res == avoided) {
            res = random.nextInt(solutionSize);
        }
        return res;
    }

    private long getDeltaScore(final List<SlideOpti> list, final List<SlideOpti> newList, final int slide1, final int slide2) {
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

    private long handleConsecutive(final List<SlideOpti> list, final List<SlideOpti> newList, final int slide1, final int slide2) {
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

    private long getReverseScore(final List<SlideOpti> list, final List<SlideOpti> newList, final int slide1, final int slide2) {
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

    private long getInsertScore(final List<SlideOpti> list, final List<SlideOpti> newList, final int slide1, final int slide2) {
        if (slide1 < slide2) {
            return getInsertScoreLower(list, newList, slide1, slide2);
        }
        else {
            return getInsertScoreHigher(list, newList, slide1, slide2);
        }
    }

    private long getInsertScoreLower(final List<SlideOpti> list, final List<SlideOpti> newList, final int slide1, final int slide2) {
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

    private long getInsertScoreHigher(final List<SlideOpti> list, final List<SlideOpti> newList, final int slide1, final int slide2) {
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


    public static void main(final String[] args) throws IOException, InterruptedException {
        Instant now = Instant.now();
        List<String> fileList = Arrays.asList("data/b_lovely_landscapes.txt",
                                              "data/c_memorable_moments.txt",
                                              "data/d_pet_pictures.txt",
                                              "data/e_shiny_selfies.txt");
        List<String> res = Arrays.asList("recuit_b.text",
                                         "recuit_c.text",
                                         "recuit_d.text",
                                         "recuit_e.text");
        Thread b = new Thread(() -> execute(fileList.get(0), res.get(0), "b"));
        Thread c = new Thread(() -> execute(fileList.get(1), res.get(1), "c"));
        Thread d = new Thread(() -> execute(fileList.get(2), res.get(2), "d"));
        Thread e = new Thread(() -> execute(fileList.get(3), res.get(3), "e"));

        //b.start();
        c.start();
        d.start();
        e.start();

        //b.join();
        c.join();
        d.join();
        e.join();
        System.out.println(Instant.now().toEpochMilli() - now.toEpochMilli());
    }

    private static void execute(final String file,
                                final String res,
                                final String name) {
        try {
            List<Photo> photos = new PhotoParser().parseData(file);
            RecuitMixed recuit = new RecuitMixed(photos, name);
            recuit.optimize(res);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
}
