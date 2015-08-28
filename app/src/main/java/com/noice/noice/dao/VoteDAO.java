package com.noice.noice.dao;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.noice.noice.model.Video;
import com.noice.noice.model.Vote;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class VoteDAO {

    public interface VoteListener {
        void onUserVoteCast();

        void onUserVoteUpdated(Vote vote);

        void onVoteCountsUpdated(int positiveCount, int negativeCount);
    }

    private List<VoteListener> listeners = new ArrayList<>();

    public void addListener(VoteListener listener) {
        listeners.add(listener);
    }

    public void removeListener(VoteListener listener) {
        listeners.remove(listener);
    }

    /**
     * Either update a user's existing vote for a video or create a new vote
     *
     * @param video video that the user is voting on
     * @param value vote value for user
     */
    public void updateOrCreateVote(@Nullable final Video video, final int value) {
        if (video == null) {
            return;
        }
        ParseQuery<Vote> query = ParseQuery.getQuery(Vote.class);
        query.whereEqualTo("user", ParseInstallation.getCurrentInstallation());
        query.whereEqualTo("video", video);
        query.getFirstInBackground(new GetCallback<Vote>() {
            public void done(Vote vote, ParseException e) {
                if (e != null) {
                    vote = new Vote();
                    vote.setCurrentInstallation();
                    vote.setVideo(video);
                }
                vote.setValue(value);
                vote.setACL(Vote.createVoteACL());
                vote.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        for (VoteListener listener : listeners) {
                            listener.onUserVoteCast();
                        }
                    }
                });
            }
        });
    }

    /**
     * Get the number of positive and negative votes for a video
     *
     * @param video video that we are getting votes for
     */
    public void getVoteCountsForVideo(@NonNull Video video) {
        new GetVoteCountsTask(video, listeners).execute();
    }

    public void getUserVoteForVideo(@NonNull Video video) {
        ParseQuery<Vote> query = ParseQuery.getQuery(Vote.class);
        query.whereEqualTo("video", video);
        query.whereEqualTo("user", ParseInstallation.getCurrentInstallation());
        query.getFirstInBackground(new GetCallback<Vote>() {
            @Override
            public void done(Vote vote, ParseException e) {
                if (e != null) {
                    return;
                }
                for (VoteListener listener : listeners) {
                    listener.onUserVoteUpdated(vote);
                }
            }
        });
    }

    private int countVotesForValue(@NonNull Video video, int value) {
        ParseQuery<Vote> query = ParseQuery.getQuery(Vote.class);
        query.whereEqualTo("video", video);
        query.whereEqualTo("value", value);
        try {
            return query.count();
        } catch (ParseException e) {
            return 0;
        }
    }

    private class GetVoteCountsTask extends AsyncTask<Void, Void, Void> {

        private int positiveCount = 0;
        private int negativeCount = 0;
        private Video mVideo;
        private List<VoteListener> listeners;

        public GetVoteCountsTask(Video video, List<VoteListener> listeners) {
            mVideo = video;
            this.listeners = listeners;
        }

        @Override
        protected Void doInBackground(Void... params) {
            positiveCount = countVotesForValue(mVideo, 1);
            negativeCount = countVotesForValue(mVideo, -1);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            for (VoteListener listener : listeners) {
                listener.onVoteCountsUpdated(positiveCount, negativeCount);
            }
        }
    }

}
