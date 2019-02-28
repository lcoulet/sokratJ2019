package com.kratos.sokratj.dumb;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class dumb1 {
    public dumb1(String filename, String path) throws IOException {
        List<String> lines = Collections.emptyList();
        try
        {
            lines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
        }

        catch (IOException e)
        {
            // do something
            e.printStackTrace();
        }

        int count = -1;
        lines = lines.subList(1, lines.size());
        List<String> newList = new ArrayList<>();
        for (String line : lines) {
            count++;
            newList.add(Integer.toString(count) + " " +line);
        }

        Collections.sort(newList, new Comparator<String>() {
            public int compare(String e1, String e2) {
                String[] ee1 = e1.split(" ");
                String[] ee2 = e2.split(" ");
                return ee1[1].compareTo(ee2[1]);
            }
        });

        List<String> buffer = new ArrayList<>();

        List<String> result = new ArrayList<>();

        for (String line : newList) {
            String[] e1 = line.split(" ");
            if (e1[1].equals("H")) {
                result.add(e1[0]);
            }
            if (e1[1].equals("V")) {
                if (buffer.size() == 2) {
                    result.add(buffer.get(0) + " " + buffer.get(1));
                    buffer.clear();
                } else {
                    buffer.add(e1[0]);
                }
            }
        }

        if (buffer.size() == 1) {
            result.add(buffer.get(0));
        }
        if (buffer.size() == 2) {
            result.add(buffer.get(0) + " " + buffer.get(1));
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Users\\consultant.jclnynfck\\Desktop\\results\\"+filename));


        writer.write(result.size()+"\n");
        for (String s : result) {
            writer.write(s+"\n");

        }
        writer.close();
    }

    public static void main(final String[] args) throws IOException {
        List<String[]> all = new ArrayList<>();
        all.add(new String[]{"example", "C:\\Users\\consultant.jclnynfck\\Documents\\Perso\\sokratJ2019\\src\\main\\resources\\data\\a_example.txt"});
        all.add(new String[]{"lovely", "C:\\Users\\consultant.jclnynfck\\Documents\\Perso\\sokratJ2019\\src\\main\\resources\\data\\b_lovely_landscapes.txt"});
        all.add(new String[]{"memorable", "C:\\Users\\consultant.jclnynfck\\Documents\\Perso\\sokratJ2019\\src\\main\\resources\\data\\c_memorable_moments.txt"});
        all.add(new String[]{"pet", "C:\\Users\\consultant.jclnynfck\\Documents\\Perso\\sokratJ2019\\src\\main\\resources\\data\\d_pet_pictures.txt"});
        all.add(new String[]{"shiny", "C:\\Users\\consultant.jclnynfck\\Documents\\Perso\\sokratJ2019\\src\\main\\resources\\data\\e_shiny_selfies.txt"});
        for (String[] a : all) {
            new dumb1(a[0], a[1]);
        }

    }
}
