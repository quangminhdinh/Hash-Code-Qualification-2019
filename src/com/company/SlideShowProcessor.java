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

    public int optimize() {
        int totalScore = 0;
        if (pool.size() == 0) {
            return 0;
        }
        Slide start = pool.remove(0);

        for (Slide slide : pool) {
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

        Slide pivot = null;
        Slide pos = null;

        while (incompleteSlides.size() != 0) {
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
        SlideShowProcessor processor = new SlideShowProcessor("d_pet_pictures.txt");
        System.out.println(processor.optimize());
    }
}
