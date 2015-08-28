package com.noice.noice.dao;

import com.noice.noice.model.Share;
import com.noice.noice.model.Video;
import com.parse.FindCallback;
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

    // Listeners to notify when the set changes.
    private List<ShareListener> listeners = new ArrayList<>();

    /**
     * Adds a listener to be notified when the set of favorites changes.
     */

    public void addListener(ShareListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener.
     */
    public void removeListener(ShareListener listener) {
        listeners.remove(listener);
    }

    public void updateOrCreateShare(final Video video) {
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


    public void updateShareCount(final Video video) {
        ParseQuery<Share> query = ParseQuery.getQuery(Share.class);
        query.whereEqualTo("video", video);
        query.findInBackground(new FindCallback<Share>() {
            @Override
            public void done(List<Share> shares, ParseException e) {
                // something went wrong
                if (e != null) {
                    return;
                }

                for (Share share : shares) {
                    // update user vote
                    if (ParseInstallation.getCurrentInstallation().hasSameId(share
                            .getInstallation())) {
                        for (ShareListener listener : listeners) {
                            listener.onUserHasShared();
                        }
                    }

                }
                for (ShareListener listener : listeners) {
                    listener.onShareCountUpdated(shares.size());
                }
            }
        });
    }

}
