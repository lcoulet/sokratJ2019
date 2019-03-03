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

public class Recuit {
    private List<SlideOpti> startSolution;
    private int doubleSlideCount;
    private int simpleSlideCount;
    private Random random;

    private List<SlideOpti> best;
    private long bestScore;

    private Map<SlideOpti, Integer> slideIntegerMap;
    private List<SlideOpti> doubleSlideList;

    private final String name;

    public Recuit(final List<Photo> photoList, String name) {
        random = new Random(System.currentTimeMillis());
        startSolution = generateSolution(photoList);
        System.out.println(Score.getScoreOpti(startSolution));
        this.name = name;
    }

    public List<SlideOpti> optimize(final String filename) throws FileNotFoundException {
        SolutionSerializer serializer = new SolutionSerializer();
        double startTemperature = 5;
        double temperature = startTemperature;
        double tau = 5000000;
        double temperatureLimit = 0.001;

        long referenceScore = Score.getScoreOpti(startSolution);
        long i = 0;

        long lastCheckScore = referenceScore;

        bestScore = referenceScore;
        best = startSolution;
        boolean swap = false;

        while (true) {
            swap = false;
            List<SlideOpti> newSolution = new ArrayList<>(startSolution);
            int firstSlide;
            int secondSlide;
            if (doubleSlideCount != 0) {
                int choice = random.nextInt(2);
                if (choice == 0) {
                    firstSlide = random.nextInt(startSolution.size());
                    secondSlide = random.nextInt(startSolution.size());
                    while (secondSlide == firstSlide) {
                        secondSlide = random.nextInt(startSolution.size());
                    }
                    Collections.swap(newSolution, firstSlide, secondSlide);
                    swap = true;
                }
                else {
                    firstSlide = getIndex(random.nextInt(doubleSlideCount));
                    secondSlide = getIndex(random.nextInt(doubleSlideCount));
                    while (secondSlide == firstSlide) {
                        secondSlide = getIndex(random.nextInt(doubleSlideCount));
                    }
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
                }
            }
            else {
                firstSlide = random.nextInt(startSolution.size());
                secondSlide = random.nextInt(startSolution.size());
                while (secondSlide == firstSlide) {
                    secondSlide = random.nextInt(startSolution.size());
                }
                Collections.swap(newSolution, firstSlide, secondSlide);
            }

            long newScore = referenceScore - getDeltaScore(startSolution, newSolution, firstSlide, secondSlide);
            if (newScore > referenceScore
                || Math.exp(-(referenceScore - newScore) / temperature) > random.nextDouble()) {
                if (swap) {
                    if (startSolution.get(firstSlide).getPhotos().size() != 1) {
                        slideIntegerMap.put(startSolution.get(firstSlide), secondSlide);
                    }
                    if (startSolution.get(secondSlide).getPhotos().size() != 1) {
                        slideIntegerMap.put(startSolution.get(secondSlide), firstSlide);
                    }
                }

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
                serializer.serializeSolutionToFileOpti(best, new File(filename));
                if (lastCheckScore == bestScore && temperature < temperatureLimit) {
                    break;
                }
                lastCheckScore = bestScore;
            }
            if (i > 1000000) {
                break;
            }
        }
        System.out.println(name + " " + bestScore + " " + Score.getScoreOpti(best) + "(" + i + ")");
        return best;
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
        //System.out.println(firstSlide + " " +secondSlide + " " + newScore + " " + previousScore);
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

    private int getIndex(final int index) {
        return slideIntegerMap.get(doubleSlideList.get(index));
    }

    private List<SlideOpti> generateSolution(final List<Photo> photoList) {
        List<PhotoOpti> optiList = PhotoParser.optimize(photoList);
        PhotoOpti verticalBuffer = null;
        simpleSlideCount = 0;
        doubleSlideCount = 0;

        slideIntegerMap = new HashMap<>();
        doubleSlideList = new ArrayList<>();

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
                    doubleSlideList.add(slide);
                    slideIntegerMap.put(slide, currentIndex);
                    verticalBuffer = null;
                    ++doubleSlideCount;
                    ++currentIndex;
                }
            }
            else {
                solutionList.add(new SlideOpti(Arrays.asList(photo), currentIndex));
                ++simpleSlideCount;
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
        //d.start();
        //e.start();

        //b.join();
        c.join();
        //d.join();
        //e.join();
        System.out.println(Instant.now().toEpochMilli() - now.toEpochMilli());
    }

    private static void execute(final String file,
                                final String res,
                                final String name) {
        try {
            List<Photo> photos = new PhotoParser().parseData(file);
            Recuit recuit = new Recuit(photos, name);
            recuit.optimize(res);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
}
