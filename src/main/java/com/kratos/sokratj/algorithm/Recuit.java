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

    public Recuit(final List<Photo> photoList) {
        random = new Random(System.currentTimeMillis());
        startSolution = generateSolution(photoList);
        System.out.println(Score.getScore(startSolution));
    }

    public List<Slide> optimize() {
        double temperature = 20;
        double temperatureStep = 0.99999;
        double temperatureLimit = 0.00001;

        long referenceScore = Score.getScore(startSolution);
        int i = 0;

        bestScore = referenceScore;
        best = startSolution;


        while (temperature > temperatureLimit) {
            List<Slide> newSolution = new ArrayList<>(startSolution);
            int firstSlide = 0;
            int secondSlide = 0;
            if (doubleSlideCount != 0) {
                int choice = random.nextInt(2);
                if (choice == 0) {
                    //move two slides
                    firstSlide = random.nextInt(startSolution.size());
                    secondSlide = random.nextInt(startSolution.size());
                    Collections.swap(newSolution, firstSlide, secondSlide);
                }
                else {
                    //System.out.println("lala");
                    //move two vertical
                    firstSlide = getIndex(startSolution, random.nextInt(doubleSlideCount));
                    secondSlide = getIndex(startSolution, random.nextInt(doubleSlideCount));
                    while (secondSlide == firstSlide) {
                        secondSlide = getIndex(startSolution, random.nextInt(doubleSlideCount));
                    }

                    int firstOne = random.nextInt(2);
                    int secondOne = random.nextInt(2);
                    SlideMutable slide1 = new SlideMutable(new ArrayList<>(startSolution.get(firstSlide).getPhotos()));
                    SlideMutable slide2 = new SlideMutable(new ArrayList<>(startSolution.get(secondSlide).getPhotos()));

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
                Collections.swap(newSolution, firstSlide, secondSlide);
            }

            long newScore = referenceScore - getDeltaScore(startSolution, newSolution, firstSlide, secondSlide);
//            long computed = Score.getScore(newSolution);
//            if (computed != newScore) {
//                System.out.println("Wrong score ");
//                System.out.println(computed + " " + newScore);
//                System.out.println(firstSlide + " " + secondSlide);
//                return null;
//            }
            if (newScore > referenceScore) {
                startSolution = newSolution;
                referenceScore = newScore;
            }
            else if (Math.exp(-(referenceScore - newScore) / temperature) > random.nextDouble()) {
                startSolution = newSolution;
                referenceScore = newScore;
            }

            if (bestScore < referenceScore) {
                bestScore = referenceScore;
                best = startSolution;
            }

            temperature = temperature * temperatureStep;

            ++i;
            if (i % 1024 == 0) {
                System.out.println(temperature + " " + referenceScore);
            }
        }
        System.out.println(bestScore + " " + Score.getScore(best));
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

    private int getIndex(final List<Slide> list, int index) {
        for (int i = 0; i < list.size(); i++) {
            Slide slide = list.get(i);
            if (slide.getPhotos().size() != 1) {
                if (index == 0) {
                    return i;
                }
                --index;
            }
        }
        return list.size() - 1;
    }

    private List<Slide> generateSolution(final List<Photo> photoList) {
        Photo verticalBuffer = null;
        simpleSlideCount = 0;
        doubleSlideCount = 0;

        List<Slide> solutionList = new ArrayList<>();

        for (Photo photo : photoList) {
            if (photo.isVertical()) {
                if (verticalBuffer == null) {
                    verticalBuffer = photo;
                }
                else {
                    solutionList.add(new SlideMutable(Arrays.asList(verticalBuffer, photo)));
                    verticalBuffer = null;
                    ++doubleSlideCount;
                }
            }
            else {
                solutionList.add(new SlideMutable(Arrays.asList(photo)));
                ++simpleSlideCount;
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
