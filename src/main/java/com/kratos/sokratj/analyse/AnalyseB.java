package com.kratos.sokratj.analyse;

import com.kratos.sokratj.model.Photo;
import com.kratos.sokratj.model.PhotoOpti;
import com.kratos.sokratj.model.SlideOptiB;
import com.kratos.sokratj.parser.PhotoParser;
import com.kratos.sokratj.utils.Score;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class AnalyseB {
    public static class Int {
        private int value;

        public Int() {
            value = 0;
        }

        public void increment() {
            ++value;
        }

        public int getValue() {
            return value;
        }
    }

    public static void makeScoreAnalysis() throws IOException {
        PhotoParser parser = new PhotoParser();
        List<Photo> photoList = parser.parseData("data/b_lovely_landscapes.txt");
        List<SlideOptiB> temp = PhotoParser.optimizeWithUnique(photoList).stream()
                .map(photoOpti -> new SlideOptiB(photoOpti, photoOpti.getId(), photoOpti.getUnique())).collect(Collectors.toList());

        List<List<Int>> scoreCountMap = new ArrayList<>();
        for (int i = 0; i < temp.size(); ++i) {
            List<Int> map = new ArrayList<>();
            scoreCountMap.add(map);
            for (long j = 1; j < 20; ++j) {
                map.add(new Int());
            }
        }


        Instant ref = Instant.now();
        for (int i = 0; i < temp.size(); ++i) {
            List<Int> map = scoreCountMap.get(i);

            for (int j = i + 1; j < temp.size(); ++j) {
                int score = (int) Score.computeScore(temp.get(i), temp.get(j));
                if (score != 0) {
                    map.get(score).increment();
                    scoreCountMap.get(j).get(score).increment();
                }
            }

            if (i % 128 == 0) {
                Instant newTime = Instant.now();
                System.out.println("lala " + (newTime.toEpochMilli() - ref.toEpochMilli()) + " (" + i + ")");
                ref = newTime;
            }
        }

        try (PrintWriter writer = new PrintWriter("analyse_b.txt")) {
            for (int i = 0; i < temp.size(); ++i) {
                StringBuilder builder = new StringBuilder();
                builder.append(i).append(" ");
                for (int j = 0; j < scoreCountMap.get(i).size(); ++j) {
                    builder.append(scoreCountMap.get(i).get(j).getValue()).append(" ");
                }
                writer.println(builder.toString());
            }

        }
    }

    public static void makeList() throws IOException {
        PhotoParser parser = new PhotoParser();
        List<Photo> photoList = parser.parseData("data/b_lovely_landscapes.txt");
        List<SlideOptiB> temp = PhotoParser.optimizeWithUnique(photoList).stream()
                .map(photoOpti -> new SlideOptiB(photoOpti, photoOpti.getId(), photoOpti.getUnique())).collect(Collectors.toList());

        List<List<Integer>> pairMap = new ArrayList<>();
        for (int i = 0; i < temp.size(); ++i) {
            pairMap.add(new ArrayList<>());
        }


        Instant ref = Instant.now();
        for (int i = 0; i < temp.size(); ++i) {
            for (int j = i + 1; j < temp.size(); ++j) {
                int score = (int) Score.computeScore(temp.get(i), temp.get(j));
                if (score != 0) {
                    pairMap.get(i).add(j);
                    pairMap.get(j).add(i);
                }
            }

            if (i % 128 == 0) {
                Instant newTime = Instant.now();
                System.out.println("lala " + (newTime.toEpochMilli() - ref.toEpochMilli()) + " (" + i + ")");
                ref = newTime;
            }
        }

        try (PrintWriter writer = new PrintWriter("pair_b.txt")) {
            for (int i = 0; i < temp.size(); ++i) {
                StringBuilder builder = new StringBuilder();
                builder.append(i).append(" ");
                for (int j = 0; j < pairMap.get(i).size(); ++j) {
                    builder.append(pairMap.get(i).get(j).intValue()).append(" ");
                }
                writer.println(builder.toString());
            }
        }
    }

    public static void main(final String[] args) throws IOException {
    }
}
