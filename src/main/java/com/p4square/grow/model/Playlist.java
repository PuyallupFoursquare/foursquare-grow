/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Representation of a user's playlist.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class Playlist {
    /**
     * Map of Chapter ID to map of Video ID to VideoRecord.
     */
    private Map<Chapters, Chapter> mPlaylist;

    private Date mLastUpdated;

    /**
     * Construct an empty playlist.
     */
    public Playlist() {
        mPlaylist = new HashMap<>();
        mLastUpdated = new Date(0); // Default to a prehistoric date if we don't have one.
    }

    /**
     * Find the VideoRecord for a video id.
     */
    public VideoRecord find(String videoId) {
        for (Chapter chapter : mPlaylist.values()) {
            VideoRecord r = chapter.getVideoRecord(videoId);

            if (r != null) {
                return r;
            }
        }

        return null;
    }

    /**
     * @param videoId The video to search for.
     * @return the Chapter containing videoId.
     */
    private Chapter findChapter(String videoId) {
        for (Chapter chapter : mPlaylist.values()) {
            VideoRecord r = chapter.getVideoRecord(videoId);

            if (r != null) {
                return chapter;
            }
        }

        return null;
    }

    /**
     * @return The last modified date of the source playlist.
     */
    public Date getLastUpdated() {
        return mLastUpdated;
    }

    /**
     * Set the last updated date.
     * @param date the new last updated date.
     */
    public void setLastUpdated(Date date) {
        mLastUpdated = date;
    }

    /**
     * Add a video to the playlist.
     */
    public VideoRecord add(Chapters chapterId, String videoId) {
        Chapter chapter = mPlaylist.get(chapterId);

        if (chapter == null) {
            chapter = new Chapter(chapterId);
            mPlaylist.put(chapterId, chapter);
        }

        VideoRecord r = new VideoRecord();
        chapter.setVideoRecord(videoId, r);
        return r;
    }

    /**
     * Add a Chapter to the Playlist.
     * @param chapterId The name of the chapter.
     * @param chapter The Chapter object to add.
     */
    public void addChapter(Chapters chapterId, Chapter chapter) {
        chapter.setName(chapterId);
        mPlaylist.put(chapterId, chapter);
    }

    /**
     * Variation of addChapter() with a String key for Jackson.
     */
    @JsonAnySetter
    private void addChapter(String chapterName, Chapter chapter) {
        addChapter(Chapters.fromString(chapterName), chapter);
    }

    /**
     * @return a map of chapter id to chapter.
     */
    @JsonAnyGetter
    public Map<Chapters, Chapter> getChaptersMap() {
        return mPlaylist;
    }

    /**
     * @return The last chapter to be completed.
     */
    @JsonIgnore
    public Map<Chapters, Boolean> getChapterStatuses() {
        Map<Chapters, Boolean> completed = new HashMap<>();

        for (Map.Entry<Chapters, Chapter> entry : mPlaylist.entrySet()) {
            completed.put(entry.getKey(), entry.getValue().isComplete());
        }

        return completed;
    }

    /**
     * @return true if all required videos in the chapter have been watched.
     */
    public boolean isChapterComplete(Chapters chapterId) {
        Chapter chapter = mPlaylist.get(chapterId);
        if (chapter != null) {
            return chapter.isComplete();
        }

        return false;
    }

    /**
     * Merge a playlist into this playlist.
     *
     * Merge is accomplished by adding all missing Chapters and VideoRecords to
     * this playlist.
     */
    public void merge(Playlist source) {
        if (source.getLastUpdated().before(mLastUpdated)) {
            // Already up to date.
            return;
        }

        for (Map.Entry<Chapters, Chapter> entry : source.getChaptersMap().entrySet()) {
            Chapters chapterName = entry.getKey();
            Chapter theirChapter = entry.getValue();
            Chapter myChapter = mPlaylist.get(entry.getKey());

            if (myChapter == null) {
                // Add new chapter
                myChapter = new Chapter(chapterName);
                addChapter(chapterName, myChapter);
            }

            // Check chapter for missing videos
            for (Map.Entry<String, VideoRecord> videoEntry : theirChapter.getVideos().entrySet()) {
                String videoId = videoEntry.getKey();
                VideoRecord myVideo = myChapter.getVideoRecord(videoId);

                if (myVideo == null) {
                    myVideo = find(videoId);
                    if (myVideo == null) {
                        // New Video
                        try {
                            myVideo = videoEntry.getValue().clone();
                            myChapter.setVideoRecord(videoId, myVideo);
                        } catch (CloneNotSupportedException e) {
                            throw new RuntimeException(e); // Unexpected...
                        }
                    } else {
                        // Video moved
                        findChapter(videoId).removeVideoRecord(videoId);
                        myChapter.setVideoRecord(videoId, myVideo);
                    }
                }
            }
        }

        mLastUpdated = source.getLastUpdated();
    }
}
