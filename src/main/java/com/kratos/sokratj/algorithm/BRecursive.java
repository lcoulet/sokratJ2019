package com.kratos.sokratj.algorithm;

import java.io.*;
import java.util.ArrayList;
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


    public static void treatData(final int depth, final int id) {
        ++call;

        solution[depth] = id;
        added[id] = true;

        if (depth == solution.length - 1) {
            throw new RuntimeException("Terminated");
        }
        if (call % 4_194_304 == 0) {
            System.out.println("Current depth " + depth);
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

        added = new boolean[data.size()];
        solution = new int[data.size()];
        for (int i = 0; i < data.size(); i++) {
            added[i] = false;
        }

        try {
            for (int i = 0; i < data.size(); ++i) {
                System.out.println("alal " + i);
                if (data.get(i).size() % 2 != 0) {
                    treatData(0, i);
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
