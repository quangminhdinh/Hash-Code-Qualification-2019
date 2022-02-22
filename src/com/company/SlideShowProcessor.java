package com.company;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class SlideShowProcessor {

    private int photosNum;

    private ArrayList<Slide> pool = new ArrayList<>();

    private ArrayList<Slide> incompleteSlides = new ArrayList<>();

    public SlideShowProcessor(String path) {
        loadData(path);
    }

    public int optimizeHalfMax() {
        int totalScore = 0;
        HashMap<Integer, ArrayList<Slide>> poolByLength = new HashMap<>();
        int totalSlides = pool.size();
        TreeSet<Integer> tagsNums = new TreeSet<>();

        for (Slide slide : pool) {
            int size = slide.getTags().size();
            tagsNums.add(size);
            poolByLength.computeIfAbsent(size, k -> new ArrayList<>());
            poolByLength.get(size).add(slide);
        }
        System.out.println("End add completed slides");
        int numLoop = incompleteSlides.size() / 2;

        for (int i = 0; i < numLoop; i++) {
            Slide start = incompleteSlides.remove(0);
            Slide end = incompleteSlides.get(0);
            int endIndex = 0;
            for (int eidx = 1; eidx < incompleteSlides.size(); eidx ++) {
                Slide slide = incompleteSlides.get(eidx);
                int common = start.getCommonTagsNum(slide);
                if (common < start.getCommonTagsNum(end)) {
                    end = slide;
                    endIndex = eidx;
                }
                if (common == 0) break;
            }
            start.mergeSlide(end);
            incompleteSlides.remove(endIndex);

            int size = start.getTags().size();
            tagsNums.add(size);
            poolByLength.computeIfAbsent(size, k -> new ArrayList<>());
            poolByLength.get(size).add(start);
            totalSlides++;
        }
        System.out.println("End add incompleted slides");
        if (totalSlides == 0 || tagsNums.isEmpty()) return 0;

        Slide pivot = null;
        int all = tagsNums.size();
        int y = 0;
        while (tagsNums.size() != 0) {
            System.out.println(y + "/" + all);
            y++;
            int highestTags = tagsNums.pollLast();
            ArrayList<Slide> currentPool = poolByLength.get(highestTags);
            while (currentPool.size() != 0) {
                int idx = 0;
                Slide maxCommon = currentPool.get(0);
                for (int k = 0; k < currentPool.size(); k++) {
                    Slide slide = currentPool.get(k);
                    if (pivot == null) {
                        pivot = currentPool.remove(k);
                        break;
                    }
                    int currentCommon = pivot.getCommonTagsNum(slide);
                    if (currentCommon > (highestTags / 2)) {
                        continue;
                    }
                    if (currentCommon > pivot.getCommonTagsNum(maxCommon)) {
                        idx = k;
                        maxCommon = slide;
                    }
                }
                if (currentPool.size() == 0) break;
                totalScore += pivot.getScore(maxCommon);
                pivot = currentPool.remove(idx);
            }
        }

//        int highestTags = tagsNums.pollLast();
//        Slide pivot = poolByLength.get(highestTags).remove(0);
//        totalSlides--;
//        if (totalSlides == 0 || tagsNums.isEmpty()) return 0;
//        if (poolByLength.get(highestTags).size() == 0) {
//            poolByLength.put(highestTags, null);
//            highestTags = tagsNums.pollLast();
//        }
//        boolean isLower = true;
//
//        for (int i = 0; i < totalSlides; i++) {
//            int currentTags = highestTags;
//            if (isLower) {
//                currentTags = highestTags / 2;
//                int increment = 0;
//                while (true) {
//                    if (currentTags <= tagsNums.first()) {
//                        currentTags = tagsNums.first();
//                        break;
//                    }
//                    if (tagsNums.contains(currentTags + increment)) {
//                        currentTags += increment;
//                        break;
//                    } else if (tagsNums.contains(currentTags - increment)) {
//                        currentTags -= increment;
//                        break;
//                    }
//                }
//            }
//            ArrayList<Slide> currentPool = poolByLength.get(currentTags);
//            Slide maxPool = currentPool.get(0);
//            int maxIdx = 0;
//            for (int k = 0; k < currentPool.size(); k++) {
//                Slide currentSlide = currentPool.get(k);
//                if (currentSlide.getCommonTagsNum(pivot) > maxPool.getCommonTagsNum(pivot)) {
//                    maxPool = currentSlide;
//                    maxIdx = k;
//                }
//                if (maxPool.getCommonTagsNum(pivot) == maxPool.getTags().size()) {
//                    break;
//                }
//            }
//            totalScore += pivot.getScore(maxPool);
//            pivot = maxPool;
//            currentPool.remove(maxIdx);
//            if (currentPool.size() == 0) {
//                poolByLength.put(currentTags, null);
//                if (i == totalSlides - 1) return totalScore;
//                if (!isLower)
//                highestTags = tagsNums.pollLast();
//                else tagsNums.remove(currentTags);
//            }
//            isLower = !isLower;
//        }
        return totalScore;
    }

    public int optimize() {
        int totalScore = 0;
        int kl = 0;
        Slide start = null;
        if (pool.size() != 0) {
//            return 0;
//        }
            start = pool.remove(0);

            for (Slide slide : pool) {
                kl++;
                System.out.println(kl + "/" + (pool.size() + incompleteSlides.size()));
                Slide maxPos = slide.getPotentialPos(start);
                if (maxPos == null) {
                    slide.setNext(start);
                    start = slide;
                    totalScore += slide.getNextScore();
                } else if (maxPos.getNext() == null) {
                    maxPos.setNext(slide);
                    totalScore += maxPos.getNextScore();
                } else {
                    totalScore += slide.getPotentialScore(maxPos);
                    slide.joinSlideShow(maxPos);
                }
            }
        }
        Slide pivot = null;
        Slide pos = null;
        if (start == null) {
            Slide tempp = incompleteSlides.remove(0);
            start = incompleteSlides.remove(0);
            start.mergeSlide(tempp);
        }
        while (incompleteSlides.size() != 0) {
            kl ++;
            System.out.println(kl + "/" + (pool.size()+incompleteSlides.size()));
            if (pivot == null) {
                pivot = incompleteSlides.remove(0);
                pos = pivot.getPotentialPos(start);
                continue;
            }

            int idMax = 0;
            Slide maxFragment = incompleteSlides.get(0);
            int maxScore = (pos == null) ? maxFragment.getScore(start) : maxFragment.getPotentialScore(pos);
            for (int i = 1; i < incompleteSlides.size(); i++) {
                Slide fragment = incompleteSlides.get(i);
                int score = (pos == null) ? fragment.getScore(start) : fragment.getPotentialScore(pos);
                if (score > maxScore) {
                    maxScore = score;
                    maxFragment = fragment;
                    idMax = i;
                }
            }

            pivot.mergeSlide(maxFragment);
            incompleteSlides.remove(idMax);

            if (pos == null) {
                pivot.setNext(start);
                start = pivot;
                totalScore += pivot.getNextScore();
            } else if (pos.getNext() == null) {
                pos.setNext(pivot);
                totalScore += pos.getNextScore();
            } else {
                totalScore += pivot.getPotentialScore(pos);
                pivot.joinSlideShow(pos);
            }
            pivot = null;
        }

        return totalScore;
    }

    private void loadData(String path) {
        String local = "src/com/company/";
        try(BufferedReader br = new BufferedReader(new FileReader(local + path))) {
            photosNum = Integer.parseInt(br.readLine());
            String line = br.readLine();

            int idx = 0;

            while (line != null) {
                String[] photoData = line.split(" ");
                Slide slide = new Slide(idx);

                for (int i = 0; i < Integer.parseInt(photoData[1]); i++) {
                    slide.addTag(photoData[2 + i]);
                }

                if (Objects.equals(photoData[0], "H")) {
                    pool.add(slide);
                } else {
                    incompleteSlides.add(slide);
                }

                line = br.readLine();
                idx++;
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
	// write your code here
        SlideShowProcessor processor = new SlideShowProcessor("c_memorable_moments.txt");
        System.out.println(processor.optimizeHalfMax());
    }
}
