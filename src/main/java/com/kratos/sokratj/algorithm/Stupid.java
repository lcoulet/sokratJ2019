package com.kratos.sokratj.algorithm;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicLongMap;
import com.kratos.sokratj.Main;
import com.kratos.sokratj.model.ImmutablePhoto;
import com.kratos.sokratj.model.Photo;
import com.kratos.sokratj.model.Slide;
import com.kratos.sokratj.model.SlideMutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Stupid
 *
 * @author Loic.Coulet
 */
public class Stupid {


    HashMultimap<String, Photo> indexedPhotos = HashMultimap.create();
    Set<Photo> allPhotosSet = new HashSet<>();
    Map<Photo, Slide> virtualPhotos = new HashMap<>();
    List<Slide> results = new ArrayList<>();

    final Logger logger = LoggerFactory.getLogger(Main.class);

    private int limitSearch=100_000;

    public List<Slide> compute(List<Photo> input) {

        logger.info("Removing worthless photos");
        input.removeIf(photo -> !photo.isVertical() && photo.getTags().size() < 2);

        logger.info("Doing vertical photos");
        List<Photo> verticalPhotos = input.stream().filter(photo -> photo.isVertical()).sorted((Comparator.comparingInt(o -> o.getTags().size()))).collect(Collectors.toList());
        buildVerticalPhotoSlides(verticalPhotos);

        logger.info("Doing horizontal photos");
        List<Photo> horizontalPhotos = input.stream().filter(photo -> !photo.isVertical()).collect(Collectors.toList());
        buildHorizontalPhotoSlides(horizontalPhotos);

        logger.info("Reindex using virtual");
        indexPhotos(virtualPhotos.keySet());

        logger.info("Ordering slides");
        processVirtualSlides();

        return results;

    }

    private void processVirtualSlides() {


        Photo currentPhoto = allPhotosSet.stream().findFirst().get();

        addSlideFromVirtualPhoto(currentPhoto, results);

        while(allPhotosSet.size() > 0 ){
            currentPhoto = getNextPhoto(currentPhoto);
            addSlideFromVirtualPhoto(currentPhoto, results);
        }

    }

    private Photo getNextPhoto(Photo currentPhoto) {
        return getPhotoWithMostCommonTags(currentPhoto);
    }

    private Photo getAnyPhotoWithCommonTag(Photo currentPhoto) {
        return getAnyPhotoWithCommonTag(getPhotosWithCommonTags(currentPhoto));
    }

    private Photo getAnyPhotoWithCommonTag(List<Photo> photosWithCommonTags) {
        List<Photo> commonTags = photosWithCommonTags;
        if (commonTags.isEmpty()) {
            return allPhotosSet.stream().findFirst().get();
        }
        return commonTags.get(0);
    }


    private void addSlideFromVirtualPhoto(Photo currentPhoto, List<Slide> results) {
        results.add(virtualPhotos.get(currentPhoto));
        if(results.size() % 100 == 0 ){
            logger.info("Done: " + results.size());
        }
        removePhoto(currentPhoto);
    }

    private void buildHorizontalPhotoSlides(List<Photo> horizontalPhotos) {
        horizontalPhotos.forEach(photo -> virtualPhotos.put(photo, new SlideMutable(Lists.newArrayList(photo))));
    }

    void addVirtualPhoto(Slide slide){
        if(slide.getPhotos().size() > 1){
            virtualPhotos.put(createVirtualPhoto(slide), slide);
        }else{
            virtualPhotos.put(slide.getPhotos().get(0), slide);
        }
        if(virtualPhotos.size() % 100 == 0 ){
            logger.info("Building virtual slides: " + virtualPhotos.size());
        }
    }

    private Photo createVirtualPhoto(Slide slide) {
        List<String> tags = new ArrayList<>();
        slide.getPhotos().forEach(p -> tags.addAll(p.getTags()));

        return ImmutablePhoto.builder()
                .tags(tags)
                .id(slide.getPhotos().get(0).getId())
                .isVertical(true).build();

    }


    private void buildVerticalPhotoSlides(List<Photo> verticalPhotos) {
        indexPhotos(verticalPhotos);
        while (verticalPhotos.size() > 1) {
            Photo photo1 = verticalPhotos.remove(verticalPhotos.size() - 1);
            Photo photo2 = getAPhotoWithLeastCommonTags(photo1, verticalPhotos);
            verticalPhotos.remove(photo2);
            Slide slide = new SlideMutable(Lists.newArrayList(photo1,photo2));
            removePhoto(photo1);
            removePhoto(photo2);
            addVirtualPhoto(slide);
        }

    }

    private Photo getPhotoWithMostCommonTags(Photo photo) {

        List<Photo> photosWithCommonTags = getPhotosWithCommonTags(photo);
        return selectHighestScorePhoto(photo, photosWithCommonTags);
    }

    private Photo selectHighestScorePhoto(Photo photo, List<Photo> photosWithCommonTags) {
        AtomicLongMap<Photo> map = AtomicLongMap.create();

        photosWithCommonTags.parallelStream().forEach(map::incrementAndGet);

        AtomicReference<Photo> selected = new AtomicReference<>();
        Optional<AbstractMap.SimpleEntry<Photo, Double>> res;
        final double bestPossibleScore=1.0/3.0;
        try{
            res = map.asMap().entrySet()
                    .parallelStream()
                    .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), score(photo, e.getKey(), e.getValue())))
                    .limit(limitSearch)
                    .map(entry -> {
                        if(bestPossibleScore - entry.getValue() < 0.01){
                            selected.set(entry.getKey());
                            throw new IllegalStateException("Got one");
                        }
                        return entry;
                    })
                    .max(Comparator.comparingDouble(AbstractMap.SimpleEntry::getValue));
        }catch(Exception e){
            return selected.get();
        }
        if(res.isPresent())return res.get().getKey();
        return allPhotosSet.stream().findFirst().get();
    }



    private Photo selectLeastFrequentPhoto(List<Photo> photosWithCommonTags, List<Photo> allPhotos) {
        AtomicLongMap<Photo> map = AtomicLongMap.create();
        photosWithCommonTags.stream().forEach(map::incrementAndGet);

        final AtomicReference<Map.Entry<Photo, Long>> minEntry = new AtomicReference<>();

        try{
            return map.asMap().entrySet().parallelStream()
                    .limit(limitSearch)
                    .map(entry -> {
                        if(entry.getValue() == 1){
                            minEntry.set(entry);
                            throw new IllegalStateException("Gotcha !");
                        }
                        return entry;
                    }).min(Comparator.comparingLong(value -> value.getValue())).get().getKey();
        }catch(Exception e){
            return minEntry.get().getKey();
        }
    }

    private double score(Photo current, Photo matching, long tagsInCommon){
        long rightRemain = matching.getTags().size() - tagsInCommon;
        long leftRemain = current.getTags().size() - tagsInCommon;
        long allTags = matching.getTags().size() + current.getTags().size();
        long score = Math.min(tagsInCommon, Math.min(leftRemain, rightRemain));
        double normalized = score/(allTags*1.0);
        return normalized;
    }

    private List<Photo> getPhotosWithCommonTags(Photo photo) {
        List<Photo> results = new ArrayList<>();
        photo.getTags().stream().forEach(tag -> results.addAll(indexedPhotos.get(tag)));

        return results;
    }

    private Set<Photo> getPhotosSetWithCommonTags(Photo photo) {
        Set<Photo> results = new HashSet<>();
        photo.getTags().stream().forEach(tag -> results.addAll(indexedPhotos.get(tag)));

        return results;
    }


    private Photo getAPhotoWithLeastCommonTags(Photo photo, List<Photo> allOthers) {
        Set<Photo> commontagsPhotoSet = getPhotosSetWithCommonTags(photo);

        // ideally there should be no tag in common AND the total of tags is even (to maximize possible score)
        Optional<Photo> optionalres = allOthers.parallelStream().filter(p-> !commontagsPhotoSet.contains(p) && (photo.getTags().size() + p.getTags().size())%2 == 0).findFirst();

        if (!optionalres.isPresent()) {
            optionalres = allOthers.parallelStream().filter(p-> !commontagsPhotoSet.contains(p)).findFirst();
        }

        if (optionalres.isPresent()) {
            return optionalres.get();
        } else {
            List<Photo> commonTagPhotos = getPhotosWithCommonTags(photo);
            return selectLeastFrequentPhoto(commonTagPhotos, allOthers);
        }

    }

    private Set<Photo> getPhotosWithNoCommonTags(Photo photo, Set<Photo> allOthers) {
        List<Photo> photosWithCommonTags = getPhotosWithCommonTags(photo);
        Set<Photo> results = new HashSet<>(allOthers);
        allOthers.removeAll(photosWithCommonTags);
        return allOthers;
    }

    public static List<Photo> sortByOccurence(List<Photo> photos) {
        AtomicLongMap<Photo> map = AtomicLongMap.create();
        photos.forEach(photo -> map.incrementAndGet(photo));

        return photos.parallelStream().sorted((o1, o2) -> Long.compare(map.get(o2), map.get(o1))).distinct().collect(Collectors.toList());
    }

    private void indexPhotos(Collection<Photo> input) {
        indexedPhotos.clear();
        allPhotosSet.clear();

        input.parallelStream().forEach(photo -> photo.getTags().stream().forEach(t -> indexedPhotos.put(t, photo)));
        allPhotosSet.addAll(input);
    }

    private void removePhoto(Photo photo) {
        photo.getTags().stream().forEach(t -> indexedPhotos.remove(t, photo));
        allPhotosSet.remove(photo);

    }
}
