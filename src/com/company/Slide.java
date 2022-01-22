package com.company;

import java.util.*;

public class Slide {

    private int idx[] = {-1, -1};

    private Set<String> tags;

    private Slide next;

    private int nextScore;

    public Slide (int idx) {
        this.idx[0] = idx;
        this.tags = new HashSet<>();
    }

    public Slide (int idx, Set<String> tags) {
        this.idx[0] = idx;
        this.tags = tags;
    }

    public int getIdx() {return idx[0];}

    public Set<String> getTags() {return tags;}

    public void setNext(Slide slide) {
        next = slide;
        nextScore = getScore(slide);
    }

    public int getNextScore() {return nextScore;}

    public Slide getNext() {return next;}

    public boolean addTag(String tag) {
        return tags.add(tag);
    }

    public void mergeSlide(Slide slide) {
        idx[1] = slide.getIdx();
        tags.addAll(slide.getTags());
    }

    public int getScore(Slide slide) {
        Set<String> commonTags = new HashSet<>(tags);
        commonTags.retainAll(slide.getTags());

        Set<String> diff1 = new HashSet<>(tags);
        diff1.removeAll(slide.getTags());

        Set<String> diff2 = new HashSet<>(slide.getTags());
        diff2.removeAll(tags);

        return Math.min(Math.min(commonTags.size(), diff1.size()), diff2.size());
    }

    public int getPotentialScore(Slide prev) {
        if (prev.getNext() == null) {
            return getScore(prev);
        }
        return getScore(prev) + getScore(prev.getNext()) - prev.getNextScore();
    }

    public Slide getPotentialPos(Slide start) {
        int maxScore = getScore(start);
        Slide maxPos = null;
        Slide pos = start;
        int score;
        while (pos.next != null) {
            score = getPotentialScore(pos);
            if (score > maxScore) {
                maxScore = score;
                maxPos = pos;
            }
            pos = pos.next;
        }
        score = getScore(pos);
        if (score > maxScore) {
            return pos;
        }
        return maxPos;
    }

    public void joinSlideShow(Slide prev) {
        setNext(prev.getNext());
        prev.setNext(this);
    }
}
