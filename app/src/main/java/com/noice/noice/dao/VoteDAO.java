package com.noice.noice.dao;

import android.support.annotation.Nullable;

import com.noice.noice.model.Video;
import com.noice.noice.model.Vote;
import com.parse.FindCallback;
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
    public void updateVoteCounts(final Video video) {
        ParseQuery<Vote> query = ParseQuery.getQuery(Vote.class);
        query.whereEqualTo("video", video);
        query.findInBackground(new FindCallback<Vote>() {
            @Override
            public void done(List<Vote> votes, ParseException e) {
                // something went wrong
                if (e != null) {
                    return;
                }

                int positive = 0;
                int negative = 0;
                for (Vote vote : votes) {
                    // update user vote
                    if (ParseInstallation.getCurrentInstallation().hasSameId(vote.getInstallation())) {
                        for (VoteListener listener : listeners) {
                            listener.onUserVoteUpdated(vote);
                        }
                    }

                    // update vote vote counts
                    if (vote.isPositive()) {
                        positive++;
                    } else if (vote.isNegative()) {
                        negative++;
                    }
                }
                for (VoteListener listener : listeners) {
                    listener.onVoteCountsUpdated(positive, negative);
                }
            }
        });
    }
}
