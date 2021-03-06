package com.kratos.sokratj.parser;

import com.kratos.sokratj.Main;
import com.kratos.sokratj.model.ImmutablePhoto;
import com.kratos.sokratj.model.Photo;
import com.kratos.sokratj.model.PhotoOpti;
import com.kratos.sokratj.model.Slide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * PhotoParser
 *
 * @author Loic.Coulet
 */
public class PhotoParser {
    final Logger logger = LoggerFactory.getLogger(Main.class);


    public List<Photo> parseData(String resourceName) throws IOException {
        logger.info("Loading file : " + resourceName);

        List<Photo> results = this.parseData(ClassLoader.getSystemResourceAsStream(resourceName));

        logger.info("Loaded " + results.size() + " photos from file : " + resourceName);
        return results;
    }

    public List<Photo> parseData(InputStream photoStream) throws IOException {

        List<Photo> results = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(photoStream));

        // number of photos
        String line = reader.readLine();
        int count = 0 ;


        while ((line = reader.readLine()) != null){
            if ( "".equals(line.trim()) ){
                break;
            }
            String[] tokens = line.split("\\s+");
            boolean vertical = "V".equals(tokens[0]);

            List<String> tags = Arrays.asList(Arrays.copyOfRange(tokens, 2, tokens.length));

            ImmutablePhoto photo = ImmutablePhoto.builder()
                    .addAllTags(tags)
                    .id(count)
                    .isVertical(vertical)
                    .build();
            results.add(photo);
            count++;

            logger.debug("   photo: " + photo);

        }



        return results;

    }

    public static List<PhotoOpti> optimize(final List<Photo> toOptimize) {
        List<PhotoOpti> res = new ArrayList<>();

        Map<String, Long> tagMap = new HashMap<>();
        long count = 0;

        for (Photo photo : toOptimize) {
            for (String s : photo.getTags()) {
                if (!tagMap.containsKey(s)) {
                    tagMap.put(s, count);
                    ++count;
                }
            }
        }

        for (Photo photo : toOptimize) {
            List<Long> translated = photo.getTags().stream().map(tagMap::get).collect(Collectors.toList());
            res.add(new PhotoOpti(translated, photo.getId(), photo.isVertical(), 0));
        }

        return res;
    }

    public static List<PhotoOpti> optimizeWithUnique(final List<Photo> toOptimize) {
        List<PhotoOpti> res = new ArrayList<>();

        Map<String, Long> tagMap = new HashMap<>();
        Map<String, Integer> countMap = new HashMap<>();
        long count = 0;

        for (Photo photo : toOptimize) {
            for (String s : photo.getTags()) {
                if (!tagMap.containsKey(s)) {
                    tagMap.put(s, count);
                    ++count;
                    countMap.put(s, 1);
                }
                else {
                    countMap.put(s, countMap.get(s) + 1);
                }
            }
        }

        for (Photo photo : toOptimize) {
            List<Long> translated = photo.getTags().stream().filter(s -> countMap.get(s) > 1).map(tagMap::get).collect(Collectors.toList());
            res.add(new PhotoOpti(translated,
                                  photo.getId(),
                                  photo.isVertical(),
                                  photo.getTags().size() - translated.size()));
        }

        return res;
    }
}
