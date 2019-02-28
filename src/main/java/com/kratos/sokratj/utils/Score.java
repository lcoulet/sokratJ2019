package com.kratos.sokratj.utils;

import com.kratos.sokratj.model.ImmutableSlide;

import java.util.List;
import java.util.stream.Collectors;

public class Score {
    public static long computeScore(final ImmutableSlide lhs,
                                   final ImmutableSlide rhs) {
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

    public static long getScore(final List<ImmutableSlide> slideList) {
        if(slideList.size() <= 1) {
            return 0;
        }

        long cumulativeSum = 0;
        for (int i = 1; i < slideList.size(); i++) {
            cumulativeSum += computeScore(slideList.get(i - 1), slideList.get(i));
        }
        return cumulativeSum;
    }
}
