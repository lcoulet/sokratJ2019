package com.kratos.sokratj.algorithm;

import com.google.common.collect.Comparators;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BRecursive {

    public static List<List<Integer>> extract(final File file) throws IOException {
        List<List<Integer>> res = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

        String line;
        while ((line = reader.readLine()) != null){
            if ( "".equals(line.trim()) ){
                break;
            }
            String[] tokens = line.split("\\s+");
            List<Integer> list = new ArrayList<>();
            for (int i = 1; i < tokens.length; ++i) {
                list.add(Integer.parseInt(tokens[i]));
            }
            res.add(list);
        }
        return res;
    }

    static boolean[] added;
    static int[] solution;
    static List<List<Integer>> data;

    static long call = 0;

    static long maxDepth = 0;
    static long minDepth = 1000000;

    static long cycle = 0;


    public static void treatData(final int depth, final int id) {
        ++call;

        solution[depth] = id;
        added[id] = true;

        if (depth > maxDepth) {
            maxDepth = depth;
        }
        if (minDepth > depth) {
            minDepth = depth;
        }
        if (depth == solution.length - 1) {
            throw new RuntimeException("Terminated");
        }
        if (call % 4_194_304 == 0) {
            if (cycle == 4) {
                throw new RuntimeException("e");
            }
            System.out.println("Current depth " + depth + " " + minDepth + " " + maxDepth + " " + maxDepth * 3);
            minDepth = 1000000;
            ++cycle;
        }

        for (Integer integer : data.get(id)) {
            if (added[integer]) {
                continue;
            }
            treatData(depth + 1, integer);
        }
        added[id] = false;
    }

    public static void main(final String[] args) throws IOException {
        data = extract(new File("pair_b_save.txt"));

        for (List<Integer> datum : data) {
            datum.sort(Comparator.comparingInt(o -> data.get(o).size()));
        }

        added = new boolean[data.size()];
        solution = new int[data.size()];
        for (int i = 0; i < data.size(); i++) {
            added[i] = false;
        }

        try {
            for (int i = 0; i < data.size(); ++i) {
                System.out.println("lala " + i);
                cycle = 0;
                maxDepth = 0;
                call = 0;
                for (int j = 0; j < data.size(); ++j) {
                    added[j] = false;
                }
                try {
                    treatData(0, i);
                }
                catch (RuntimeException e) {

                }
            }
        }
        catch (Exception e) {
            System.out.println("lala");
            try (PrintWriter writer = new PrintWriter("best_b.txt")) {
                for (int i = 0; i < solution.length; ++i) {
                    writer.println("" + solution[i]);
                }
            }
        }


    }
}
