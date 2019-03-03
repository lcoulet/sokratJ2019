package com.kratos.sokratj.utils;

import com.kratos.sokratj.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Score {
    public static long computeScore(final Slide lhs,
                                   final Slide rhs) {
        List<String> leftTags = lhs.getPhotos()
                                 .stream()
                                 .flatMap(photo -> photo.getTags().stream()).distinct().collect(Collectors.toList());
        List<String> rightTags = rhs.getPhotos().stream()
                                 .flatMap(photo -> photo.getTags().stream())
                                 .distinct()
                                 .collect(Collectors.toList());

        long lhsExclusive = leftTags.stream().filter(s -> !rightTags.contains(s)).count();
        long common = leftTags.size() - lhsExclusive;
        long rhsExclusive = rightTags.size() - common;

        return Math.min(Math.min(lhsExclusive, common), rhsExclusive);
    }

    public static long computeScore(final SlideOpti lhs,
                                    final SlideOpti rhs) {
        List<Long> leftTags = lhs.getPhotos()
                .stream()
                .flatMap(photo -> photo.getTags().stream()).distinct().collect(Collectors.toList());
        List<Long> rightTags = rhs.getPhotos().stream()
                .flatMap(photo -> photo.getTags().stream())
                .distinct()
                .collect(Collectors.toList());

        long lhsExclusive = leftTags.stream().filter(s -> !rightTags.contains(s)).count();
        long common = leftTags.size() - lhsExclusive;
        long rhsExclusive = rightTags.size() - common;

        return Math.min(Math.min(lhsExclusive, common), rhsExclusive);
    }

    public static long computeScore(final SlideOptiB lhs,
                                   final SlideOptiB rhs) {
        List<Long> leftTags = lhs.getPhoto().getTags();
        List<Long> rightTags = rhs.getPhoto().getTags();

        long lhsExclusive = leftTags.stream().filter(s -> !rightTags.contains(s)).count();
        long common = leftTags.size() - lhsExclusive;
        long rhsExclusive = rightTags.size() - common;

        return Math.min(Math.min(lhsExclusive + lhs.getUnique(), common), rhsExclusive + rhs.getUnique());
    }

    public static long getScore(final List<Slide> slideList) {
        if(slideList.size() <= 1) {
            return 0;
        }

        long cumulativeSum = 0;
        for (int i = 1; i < slideList.size(); i++) {
            cumulativeSum += computeScore(slideList.get(i - 1), slideList.get(i));
        }
        return cumulativeSum;
    }

    public static long getScoreOpti(final List<SlideOpti> slideList) {
        if(slideList.size() <= 1) {
            return 0;
        }

        long cumulativeSum = 0;
        for (int i = 1; i < slideList.size(); i++) {
            cumulativeSum += computeScore(slideList.get(i - 1), slideList.get(i));
        }
        return cumulativeSum;
    }

    public static long getScoreOptiB(final List<SlideOptiB> slideList) {
        if(slideList.size() <= 1) {
            return 0;
        }

        long cumulativeSum = 0;
        for (int i = 1; i < slideList.size(); i++) {
            cumulativeSum += computeScore(slideList.get(i - 1), slideList.get(i));
        }
        return cumulativeSum;
    }

    public static long maximalTheoricalScore(List<Photo> photos){
        return photos.stream().mapToLong(value -> value.getTags().size()).sum();
    }

    public static void checkResults(final List<Slide> slideList) {
        AtomicInteger count = new AtomicInteger(0);
        final Logger logger = LoggerFactory.getLogger(Score.class);
        for(Slide slide: slideList){
            if(slide.getPhotos().size()>1){
                slide.getPhotos().forEach(photo -> {
                    if (!photo.isVertical()) {
                        logger.error("Error at slide " + count.get() + " photo " + photo.getId() + " is not vertical...");
                    }
                });
            }
            count.incrementAndGet();
        }
    }
}
