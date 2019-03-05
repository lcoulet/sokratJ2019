package com.kratos.sokratj.algorithm;

import com.kratos.sokratj.SolutionSerializer;
import com.kratos.sokratj.model.PhotoOpti;
import com.kratos.sokratj.model.SlideOpti;
import com.kratos.sokratj.model.SlideOptiB;
import com.kratos.sokratj.parser.PhotoParser;
import com.kratos.sokratj.utils.Score;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.kratos.sokratj.algorithm.BRecursive.extract;
import static com.kratos.sokratj.algorithm.BRecursive.solution;

public class OnlyBWithAnalyze {

    static List<List<Integer>> data;

    private static long getScore(int left, int right) {
        if (data.get(left).contains(right)) {
            return 3;
        }
        return 0;
    }

    public static int getPhotoIndex(final int id, List<OnlyForB.PhotoList> list) {
        for (int i = 0; i < list.size(); i++) {
            for (SlideOptiB slideOptiB : list.get(i).slideOpti) {
                if (slideOptiB.getPhoto().getId() == id) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static void doStuff() throws IOException {
        PhotoParser parser = new PhotoParser();
        List<PhotoOpti> temp = PhotoParser.optimizeWithUnique(parser.parseData("data/b_lovely_landscapes.txt"));
        List<OnlyForB.PhotoList> photoList = new ArrayList<>();
        for (PhotoOpti photoOpti : temp) {
            photoList.add(new OnlyForB.PhotoList(new SlideOptiB(photoOpti, 1, photoOpti.getUnique())));
        }

        //we merge the only possibility document
        for (int i = 0; i < data.size(); ++i) {
            if (i % 256 == 0) {
                System.out.println(i + " " + photoList.size());
            }
            if (data.get(i).size() == 2) {
                int middleIndex = getPhotoIndex(i, photoList);
                OnlyForB.PhotoList middle = photoList.get(middleIndex);
                if (middle.slideOpti.size() != 1) {
                    boolean isLeft = middle.slideOpti.get(0).getPhoto().getId() == i;
                    int index = getPhotoIndex(data.get(i).get(0), photoList);
                    if (index == middleIndex) {
                        index = getPhotoIndex(data.get(i).get(1), photoList);
                    }
                    if (isLeft) {
                        if (photoList.get(index).slideOpti.get(0).getId() == data.get(i).get(0)) {
                            middle.addLeftReverse(photoList.get(index));
                        }
                        else {
                            middle.addLeft(photoList.get(index));
                        }
                    }
                    else {
                        if (photoList.get(index).slideOpti.get(0).getId() == data.get(i).get(0)) {
                            middle.addRight(photoList.get(index));
                        }
                        else {
                            middle.addRightReverse(photoList.get(index));
                        }
                    }
                    photoList.remove(index);
                }
                else {
                    int index = getPhotoIndex(data.get(i).get(0), photoList);
                    if (photoList.get(index).slideOpti.get(0).getId() == data.get(i).get(0)) {
                        middle.addLeftReverse(photoList.get(index));
                    }
                    else {
                        middle.addLeft(photoList.get(index));
                    }
                    photoList.remove(index);

                    index = getPhotoIndex(data.get(i).get(1), photoList);
                    if (photoList.get(index).slideOpti.get(0).getId() == data.get(i).get(0)) {
                        middle.addRight(photoList.get(index));
                    }
                    else {
                        middle.addRightReverse(photoList.get(index));
                    }
                    photoList.remove(index);
                }
            }
        }

        Random random = new Random(System.currentTimeMillis());
        Instant refTime = Instant.now();
        long accScore = 0;
        boolean noMore = false;

        while (photoList.size() != 1) {
            long bestScore = -1;
            boolean left = false;
            boolean reverse = false;
            int indexLeft = -1;
            int indexRight = -1;

            int selected;
            if (!noMore) {
                int min = Integer.MAX_VALUE;
                int found = -1;
                for (int i = 0; i < photoList.size(); ++i) {
                    if (photoList.get(i).slideOpti.size() == 1) {
                        if (data.get(photoList.get(i).slideOpti.get(0).getId()).size() < min) {
                            found = i;
                            min = photoList.get(i).slideOpti.size();
                        }
                    }
                }
                if (found == -1) {
                    noMore = true;
                    found = random.nextInt(photoList.size());
                }
                selected = found;
            }
            else {
                selected = random.nextInt(photoList.size());
            }

            int refIndex = selected;
            OnlyForB.PhotoList lala = photoList.get(refIndex);

            for (int j = 0; j < photoList.size(); j++) {
                if (refIndex == j) {
                    continue;
                }
                long score = getScore(lala.getLeft().getPhoto().getId(), photoList.get(j).getRight().getPhoto().getId());
                if (score > bestScore) {
                    bestScore = score;
                    indexLeft = j;
                    indexRight = refIndex;
                    left = true;
                    reverse = false;
                }
                if (lala.slideOpti.size() != 1 || photoList.get(j).slideOpti.size() != 1) {
                    score = getScore(lala.getRight().getPhoto().getId(), photoList.get(j).getLeft().getPhoto().getId());
                    if (score > bestScore) {
                        bestScore = score;
                        indexLeft = refIndex;
                        indexRight = j;
                        left = false;
                        reverse = false;
                    }

                    score = getScore(lala.getLeft().getPhoto().getId(), photoList.get(j).getLeft().getPhoto().getId());
                    if (score > bestScore) {
                        bestScore = score;
                        indexLeft = refIndex;
                        indexRight = j;
                        left = true;
                        reverse = true;
                    }

                    score = getScore(lala.getRight().getPhoto().getId(), photoList.get(j).getRight().getPhoto().getId());
                    if (score > bestScore) {
                        bestScore = score;
                        indexLeft = refIndex;
                        indexRight = j;
                        left = false;
                        reverse = true;
                    }
                }
            }

            accScore += bestScore;

            if (left) {
                if (!reverse) {
                    OnlyForB.PhotoList ref = photoList.get(indexRight);
                    ref.addLeft(photoList.get(indexLeft));
                    photoList.remove(indexLeft);
                }
                else {
                    OnlyForB.PhotoList ref = photoList.get(indexRight);
                    ref.addLeftReverse(photoList.get(indexLeft));
                    photoList.remove(indexLeft);
                }

            }
            else {
                if (!reverse) {
                    OnlyForB.PhotoList ref = photoList.get(indexLeft);
                    ref.addRight(photoList.get(indexRight));
                    photoList.remove(indexRight);
                }
                else {
                    OnlyForB.PhotoList ref = photoList.get(indexLeft);
                    ref.addRightReverse(photoList.get(indexRight));
                    photoList.remove(indexRight);
                }
            }
            if (photoList.size() % 128 == 0) {
                Instant newRef = Instant.now();
                System.out.println("lala " + photoList.size() + " " + (newRef.toEpochMilli() - refTime.toEpochMilli()) + " " + accScore);
                refTime = newRef;
            }

        }

        System.out.println(Score.getScoreOptiB(photoList.get(0).slideOpti));
        SolutionSerializer solutionSerializer = new SolutionSerializer();
        solutionSerializer.serializeSolutionToFileOptiB(photoList.get(0).slideOpti, new File("res_opti_b_analysis.txt"));

        //RecuitMixedOnlyB onlyB = new RecuitMixedOnlyB(photoList.get(0).slideOpti, "b");
        //onlyB.optimize("opti_and_recuit_mixed_b.txt");
    }

    public static void main(final String[] args) throws IOException {
        data = extract(new File("pair_b_save.txt"));
        doStuff();
    }


}
