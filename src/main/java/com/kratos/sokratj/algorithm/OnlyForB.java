package com.kratos.sokratj.algorithm;

import com.kratos.sokratj.SolutionSerializer;
import com.kratos.sokratj.model.Photo;
import com.kratos.sokratj.model.PhotoOpti;
import com.kratos.sokratj.model.SlideOpti;
import com.kratos.sokratj.model.SlideOptiB;
import com.kratos.sokratj.parser.PhotoParser;
import com.kratos.sokratj.utils.Score;
import sun.font.CoreMetrics;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class OnlyForB {
    public static class PhotoList {
        public List<SlideOptiB> slideOpti;

        public PhotoList(final SlideOptiB photo) {
            slideOpti = new ArrayList<>();
            slideOpti.add(photo);
        }

        public void addLeft(final PhotoList toAdd) {
            slideOpti.addAll(0, toAdd.slideOpti);
        }

        public void addLeftReverse(final PhotoList toAdd) {
            Collections.reverse(toAdd.slideOpti);
            slideOpti.addAll(0, toAdd.slideOpti);
        }

        public void addRight(final PhotoList toAdd) {
            slideOpti.addAll(toAdd.slideOpti);
        }

        public void addRightReverse(final PhotoList toAdd) {
            Collections.reverse(toAdd.slideOpti);
            slideOpti.addAll(toAdd.slideOpti);
        }

        public SlideOptiB getLeft() {
            return slideOpti.get(0);
        }

        public SlideOptiB getRight() {
            return slideOpti.get(slideOpti.size() - 1);
        }
    }

    private List<PhotoList> photoList;

    public OnlyForB(final List<Photo> content) {
        List<PhotoOpti> temp = PhotoParser.optimizeWithUnique(content);

        photoList = new ArrayList<>();
        for (PhotoOpti photoOpti : temp) {
            photoList.add(new PhotoList(new SlideOptiB(photoOpti, 1, photoOpti.getUnique())));
        }
        Random random = new Random(System.currentTimeMillis());
        Instant refTime = Instant.now();
        long accScore = 0;

        while (photoList.size() != 1) {
            long bestScore = -1;
            boolean left = false;
            boolean reverse = false;
            int indexLeft = -1;
            int indexRight = -1;

            int i = random.nextInt(photoList.size());
            PhotoList lala = photoList.get(i);

            for (int j = 0; j < photoList.size(); j++) {
                if (i == j) {
                    continue;
                }
                long score = Score.computeScore(lala.getLeft(), photoList.get(j).getRight());
                if (score > bestScore) {
                    bestScore = score;
                    indexLeft = j;
                    indexRight = i;
                    left = true;
                    reverse = false;
                }
                if (lala.slideOpti.size() != 1 || photoList.get(j).slideOpti.size() != 1) {
                    score = Score.computeScore(lala.getRight(), photoList.get(j).getLeft());
                    if (score > bestScore) {
                        bestScore = score;
                        indexLeft = i;
                        indexRight = j;
                        left = false;
                        reverse = false;
                    }

                    score = Score.computeScore(lala.getLeft(), photoList.get(j).getLeft());
                    if (score > bestScore) {
                        bestScore = score;
                        indexLeft = i;
                        indexRight = j;
                        left = true;
                        reverse = true;
                    }

                    score = Score.computeScore(lala.getRight(), photoList.get(j).getRight());
                    if (score > bestScore) {
                        bestScore = score;
                        indexLeft = i;
                        indexRight = j;
                        left = false;
                        reverse = true;
                    }
                }
            }

            accScore += bestScore;

            if (left) {
                if (!reverse) {
                    PhotoList ref = photoList.get(indexRight);
                    ref.addLeft(photoList.get(indexLeft));
                    photoList.remove(indexLeft);
                }
                else {
                    PhotoList ref = photoList.get(indexRight);
                    ref.addLeftReverse(photoList.get(indexLeft));
                    photoList.remove(indexLeft);
                }

            }
            else {
                if (!reverse) {
                    PhotoList ref = photoList.get(indexLeft);
                    ref.addRight(photoList.get(indexRight));
                    photoList.remove(indexRight);
                }
                else {
                    PhotoList ref = photoList.get(indexLeft);
                    ref.addRightReverse(photoList.get(indexRight));
                    photoList.remove(indexRight);
                }
            }
            if (photoList.size() % 32 == 0) {
                Instant newRef = Instant.now();
                System.out.println("lala " + photoList.size() + " " + (newRef.toEpochMilli() - refTime.toEpochMilli()) + " " + accScore);
                refTime = newRef;
            }

        }
    }


    public static void main(final String[] args) throws IOException {
        PhotoParser parser = new PhotoParser();
        OnlyForB onlyForB = new OnlyForB(parser.parseData("data/b_lovely_landscapes.txt"));
        System.out.println(Score.getScoreOptiB(onlyForB.photoList.get(0).slideOpti));
        SolutionSerializer solutionSerializer = new SolutionSerializer();
        solutionSerializer.serializeSolutionToFileOptiB(onlyForB.photoList.get(0).slideOpti, new File("res_opti_b.txt"));
    }
}
