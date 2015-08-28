package com.noice.noice.dao;

import android.support.annotation.NonNull;

import com.noice.noice.model.Share;
import com.noice.noice.model.Video;
import com.parse.CountCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class ShareDAO {

    public interface ShareListener {
        void onUserHasShared();

        void onShareCountUpdated(int count);
    }

    private List<ShareListener> listeners = new ArrayList<>();

    public void addListener(ShareListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ShareListener listener) {
        listeners.remove(listener);
    }

    public void updateOrCreateShare(@NonNull final Video video) {
        ParseQuery<Share> query = ParseQuery.getQuery(Share.class);
        query.whereEqualTo("user", ParseInstallation.getCurrentInstallation());
        query.whereEqualTo("video", video);
        query.getFirstInBackground(new GetCallback<Share>() {
            public void done(Share share, ParseException e) {
                // only add share if it did not already exist
                if (e != null) {
                    share = new Share();
                    share.setCurrentInstallation();
                    share.setVideo(video);
                    share.setACL(Share.createShareACL());
                    share.saveInBackground();
                }
            }
        });
    }


    public void getShareCount(@NonNull final Video video) {
        ParseQuery<Share> query = ParseQuery.getQuery(Share.class);
        query.whereEqualTo("video", video);
        query.countInBackground(new CountCallback() {
            @Override
            public void done(int i, ParseException e) {
                for (ShareListener listener : listeners) {
                    listener.onShareCountUpdated(i);
                }
            }
        });
    }

    public void getUserShareState(@NonNull final Video video) {
        ParseQuery<Share> query = ParseQuery.getQuery(Share.class);
        query.whereEqualTo("video", video);
        query.whereEqualTo("user", ParseInstallation.getCurrentInstallation());
        query.getFirstInBackground(new GetCallback<Share>() {
            @Override
            public void done(Share vote, ParseException e) {
                if (e != null) {
                    return;
                }
                for (ShareListener listener : listeners) {
                    listener.onUserHasShared();
                }
            }
        });
    }

}
