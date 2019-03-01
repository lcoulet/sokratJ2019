package com.kratos.sokratj.algorithm;

import com.kratos.sokratj.SolutionSerializer;
import com.kratos.sokratj.model.Photo;
import com.kratos.sokratj.model.Slide;
import com.kratos.sokratj.model.SlideMutable;
import com.kratos.sokratj.parser.PhotoParser;
import com.kratos.sokratj.utils.Score;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Recuit {
    private List<Slide> startSolution;
    private int doubleSlideCount;
    private int simpleSlideCount;
    private Random random;

    private List<Slide> best;
    private long bestScore;

    private Map<Slide, Integer> slideIntegerMap;
    private List<Slide> doubleSlideList;

    private final String name;

    public Recuit(final List<Photo> photoList, String name) {
        random = new Random(System.currentTimeMillis());
        startSolution = generateSolution(photoList);
        System.out.println(Score.getScore(startSolution));
        this.name = name;
    }

    public List<Slide> optimize() {
        double startTemperature = 10;
        double temperature = startTemperature;
        double tau = 1000000;
        double temperatureLimit = 0.00001;

        long referenceScore = Score.getScore(startSolution);
        long i = 0;

        bestScore = referenceScore;
        best = startSolution;
        boolean swap = false;

        while (temperature > temperatureLimit) {
            swap = false;
            List<Slide> newSolution = new ArrayList<>(startSolution);
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
                    SlideMutable slide1 = new SlideMutable(new ArrayList<>(startSolution.get(firstSlide).getPhotos()),
                                                           startSolution.get(firstSlide).getId());
                    SlideMutable slide2 = new SlideMutable(new ArrayList<>(startSolution.get(secondSlide).getPhotos()),
                                                           startSolution.get(secondSlide).getId());

                    Photo temp = startSolution.get(firstSlide).getPhotos().get(firstOne);
                    Photo temp2 = startSolution.get(secondSlide).getPhotos().get(secondOne);
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
            if (i % 200000 == 0) {
                System.out.println(name + " " + temperature + " " + referenceScore);
            }
        }
        System.out.println(name + " " + bestScore + " " + Score.getScore(best) + "(" + i + ")");
        return best;
    }

    private long getDeltaScore(final List<Slide> list, final List<Slide> newList, final int slide1, final int slide2) {
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

    private long handleConsecutive(final List<Slide> list, final List<Slide> newList, final int slide1, final int slide2) {
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

    private List<Slide> generateSolution(final List<Photo> photoList) {
        Photo verticalBuffer = null;
        simpleSlideCount = 0;
        doubleSlideCount = 0;

        slideIntegerMap = new HashMap<>();
        doubleSlideList = new ArrayList<>();

        List<Slide> solutionList = new ArrayList<>();

        int currentIndex = 0;

        for (Photo photo : photoList) {
            if (photo.isVertical()) {
                if (verticalBuffer == null) {
                    verticalBuffer = photo;
                }
                else {
                    Slide slide = new SlideMutable(Arrays.asList(verticalBuffer, photo), currentIndex);
                    solutionList.add(slide);
                    doubleSlideList.add(slide);
                    slideIntegerMap.put(slide, currentIndex);
                    verticalBuffer = null;
                    ++doubleSlideCount;
                    ++currentIndex;
                }
            }
            else {
                solutionList.add(new SlideMutable(Arrays.asList(photo), currentIndex));
                ++simpleSlideCount;
                ++currentIndex;
            }
        }
        return solutionList;
    }


    public static void main(final String[] args) throws IOException, InterruptedException {
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

        b.start();
        c.start();
        d.start();
        e.start();

        b.join();
        c.join();
        d.join();
        e.join();
    }

    private static void execute(final String file,
                                final String res,
                                final String name) {
        try {
            List<Photo> photos = new PhotoParser().parseData(file);
            Recuit recuit = new Recuit(photos, name);
            SolutionSerializer serializer = new SolutionSerializer();
            serializer.serializeSolutionToFile(recuit.optimize(), new File(res));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
}
