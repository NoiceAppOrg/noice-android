package com.noice.noice.dao;

import android.os.Handler;
import android.os.Looper;
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

import bolts.Continuation;
import bolts.Task;

public class VoteDAO {

    public interface VoteListener {
        void onUserVoteCast();

        void onUserVoteUpdated(Vote vote);

        void onVoteCountsUpdated(int positiveCount, int negativeCount);
    }

    // Listeners to notify when the set changes.
    private List<VoteListener> listeners = new ArrayList<>();


    /**
     * Adds a listener to be notified when the set of favorites changes.
     */
    public void addListener(VoteListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener.
     */
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
    public void updateVoteCounts(@NonNull Video video) {
        ArrayList<Task<Integer>> tasks = new ArrayList<>();
        tasks.add(createVoteTaskForValue(video, 1));
        tasks.add(createVoteTaskForValue(video, -1));
        Task.whenAllResult(tasks).onSuccess(new Continuation<List<Integer>, Object>() {
            @Override
            public Object then(final Task<List<Integer>> task) throws Exception {
                for (final VoteListener listener : listeners) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onVoteCountsUpdated(task.getResult().get(0), task.getResult
                                    ().get(1));
                        }
                    });
                }
                return null;
            }
        });

        // get user vote
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

    private Task<Integer> createVoteTaskForValue(Video video, int value) {
        ParseQuery<Vote> query = ParseQuery.getQuery(Vote.class);
        query.whereEqualTo("video", video);
        query.whereEqualTo("value", value);
        return query.countInBackground();
    }

}
