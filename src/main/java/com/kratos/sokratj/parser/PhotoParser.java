package com.kratos.sokratj.parser;

import com.kratos.sokratj.Main;
import com.kratos.sokratj.model.ImmutablePhoto;
import com.kratos.sokratj.model.Photo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

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
            boolean vertical = "V".equalsIgnoreCase(tokens[0]);

            List<String> tags = Arrays.asList(Arrays.copyOfRange(tokens, 1, tokens.length));

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
}
