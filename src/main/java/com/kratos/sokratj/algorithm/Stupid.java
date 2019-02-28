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

    public List<Slide> compute(List<Photo> input) {

        indexPhotos(input);

        input.removeIf(photo -> !photo.isVertical() && photo.getTags().size() < 2);

        List<Photo> verticalPhotos = input.stream().filter(photo -> photo.isVertical()).sorted((Comparator.comparingInt(o -> o.getTags().size()))).collect(Collectors.toList());

        logger.info("Doing vertical photos");
        buildVerticalPhotoSlides(verticalPhotos);
        logger.info("Doing horizontal photos");
        buildHorizontalPhotoSlides();
        logger.info("Ordering slides");
        processVirtualSlides();

        return results;

    }

    private void processVirtualSlides() {
        indexPhotos(virtualPhotos.keySet());

        Photo currentPhoto = allPhotosSet.stream().findFirst().get();

        addSlideFromVirtualPhoto(currentPhoto, results);

        while(allPhotosSet.size() > 0 ){
            addSlideFromVirtualPhoto(getNextPhoto(currentPhoto), results);
        }

    }

    private Photo getNextPhoto(Photo currentPhoto) {
        List<Photo> commonTags = sortByOccurence(getPhotosWithCommonTags(currentPhoto));
        if(commonTags.isEmpty()){
            return allPhotosSet.stream().findFirst().get();
        }
        return commonTags.get(commonTags.size()/2);
    }


    private void addSlideFromVirtualPhoto(Photo currentPhoto, List<Slide> results) {
        results.add(virtualPhotos.get(currentPhoto));
        if(results.size() % 100 == 0 ){
            logger.info("Done: " + results.size());
        }
        removePhoto(currentPhoto);
    }

    private void buildHorizontalPhotoSlides() {
        allPhotosSet.forEach(photo -> virtualPhotos.put(photo, new SlideMutable(Lists.newArrayList(photo))));
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

    private List<Photo> getPhotosWithCommonTags(Photo photo) {
        List<Photo> results = new ArrayList<>();
        photo.getTags().stream().forEach(tag -> results.addAll(indexedPhotos.get(tag)));

        return results;
    }

    private Photo getPhotoWithMostCommonTags(Photo photo) {
        List<Photo> commonTags = sortByOccurence(getPhotosWithCommonTags(photo));
        if(commonTags.isEmpty()){
            return allPhotosSet.stream().findFirst().get();
        }
        return commonTags.get(0);
    }


    private Photo getAPhotoWithLeastCommonTags(Photo photo, List<Photo> allOthers) {

        List<Photo> commonTagPhotos = getPhotosWithCommonTags(photo);

        Optional<Photo> optionalres = allOthers.stream().filter(p-> commonTagPhotos.contains(p)).findFirst();

        if (optionalres.isPresent()) {
            return optionalres.get();
        } else {
            List<Photo> data = sortByOccurence(commonTagPhotos);
            return data.get(data.size() - 1);
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

        return photos.stream().sorted((o1, o2) -> Long.compare(map.get(o2), map.get(o1))).distinct().collect(Collectors.toList());
    }

    private void indexPhotos(Collection<Photo> input) {
        indexedPhotos.clear();
        allPhotosSet.clear();

        input.stream().forEach(photo -> photo.getTags().stream().forEach(t -> indexedPhotos.put(t, photo)));
        allPhotosSet.addAll(input);
    }

    private void removePhoto(Photo photo) {
        photo.getTags().stream().forEach(t -> indexedPhotos.remove(t, photo));
        allPhotosSet.remove(photo);

    }
}
