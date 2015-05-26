package com.noice.noice.dao;

import com.noice.noice.model.Video;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class VideoDAO {

    public interface VideoListener {
        void onVideoReceived(Video video);
    }

    // Listeners to notify when the set changes.
    private List<VideoListener> listeners = new ArrayList<>();

    /**
     * Adds a listener to be notified when the set of favorites changes.
     */

    public void addListener(VideoListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener.
     */
    public void removeListener(VideoListener listener) {
        listeners.remove(listener);
    }

    public void getMostRecentInBackground() {
        ParseQuery<Video> query = ParseQuery.getQuery(Video.class);
        query.orderByDescending("createdAt");
        query.getFirstInBackground(new GetCallback<Video>() {
            @Override
            public void done(Video video, ParseException e) {
                if (e == null) {
                    for (VideoListener listener : listeners) {
                        listener.onVideoReceived(video);
                    }
                }
            }
        });
    }
}
