package com.noice.noice.view.model;

import com.noice.noice.dao.ShareDAO;
import com.noice.noice.dao.VideoDAO;
import com.noice.noice.dao.VoteDAO;
import com.noice.noice.model.Video;
import com.noice.noice.model.Vote;

public class VideoViewModel implements VoteDAO.VoteListener, VideoDAO
        .VideoListener, ShareDAO.ShareListener {

    public interface VideoViewModelListener {
        void onVideoLoaded(VideoViewModel videoViewModel);
    }

    private VideoViewModelListener listener;

    // DAOs
    private VideoDAO mVideoDAO = new VideoDAO();
    private VoteDAO mVoteDAO = new VoteDAO();
    private ShareDAO mShareDAO = new ShareDAO();
    private Video mVideo;

    @Vote.VoteType
    private int userVote = Vote.VOTE_NONE;
    private boolean hasShared = false;
    private int mPositiveVoteCount = 0;
    private int mNegativeVoteCount = 0;
    private int mShareCount = 0;
    private boolean isVideoLoaded = false;
    private boolean isVoteLoaded = false;
    private boolean isShareLoaded = false;

    public Video getVideo() {
        return mVideo;
    }

    public int getUserVote() {
        return userVote;
    }

    public boolean hasShared() {
        return hasShared;
    }

    public int getPositiveVoteCount() {
        return mPositiveVoteCount;
    }

    public int getNegativeVoteCount() {
        return mNegativeVoteCount;
    }

    public int getShareCount() {
        return mShareCount;
    }

    public boolean isVideoLoaded() {
        return isVideoLoaded;
    }

    public void setVideoLoaded(boolean isVideoLoaded) {
        this.isVideoLoaded = isVideoLoaded;
    }

    public boolean isVoteLoaded() {
        return isVoteLoaded;
    }

    public void setVoteLoaded(boolean isVoteLoaded) {
        this.isVoteLoaded = isVoteLoaded;
    }

    public boolean isShareLoaded() {
        return isShareLoaded;
    }

    public void setShareLoaded(boolean isShareLoaded) {
        this.isShareLoaded = isShareLoaded;
    }

    public void addListener(VideoViewModelListener listener) {
        this.listener = listener;
        mVideoDAO.addListener(this);
        mVoteDAO.addListener(this);
        mShareDAO.addListener(this);
    }

    public void removeListener() {
        this.listener = null;
        mVideoDAO.removeListener(this);
        mVoteDAO.removeListener(this);
        mShareDAO.removeListener(this);
    }

    public void getTodaysVideo() {
        resetUserState();
        mVideoDAO.getMostRecentInBackground();
    }

    public void getRandomVideo() {
        resetUserState();
        mVideoDAO.getRandomInBackground();
    }

    public void likeVideo() {
        if (mVideo != null) {
            updateVoteCountsBasedOnUserVote(userVote, Vote.VOTE_POSITIVE);
            userVote = Vote.VOTE_POSITIVE;
            mVoteDAO.updateOrCreateVote(mVideo, 1);
            notifyListener();
        }
    }

    public void dislikeVideo() {
        if (mVideo != null) {
            updateVoteCountsBasedOnUserVote(userVote, Vote.VOTE_NEGATIVE);
            userVote = Vote.VOTE_NEGATIVE;
            mVoteDAO.updateOrCreateVote(mVideo, -1);
            notifyListener();
        }
    }

    private void updateVoteCountsBasedOnUserVote(int oldValue, int newValue) {
        if (oldValue == Vote.VOTE_NEGATIVE) {
            mNegativeVoteCount--;
        } else if (oldValue == Vote.VOTE_POSITIVE) {
            mPositiveVoteCount--;
        }

        if (newValue == Vote.VOTE_NEGATIVE) {
            mNegativeVoteCount++;
        } else if (newValue == Vote.VOTE_POSITIVE) {
            mPositiveVoteCount++;
        }
    }

    public void share() {
        mShareDAO.updateOrCreateShare(mVideo);
        if (!hasShared) {
            mShareCount++;
            hasShared = true;
        }
        notifyListener();
    }

    private void notifyListener() {
        listener.onVideoLoaded(this);
    }

    private void resetUserState() {
        userVote = Vote.VOTE_NONE;
        hasShared = false;
    }

    @Override
    public void onVoteCountsUpdated(int positiveCount, int negativeCount) {
        mPositiveVoteCount = positiveCount;
        mNegativeVoteCount = negativeCount;
        isVoteLoaded = true;
        notifyListener();
    }

    @Override
    public void onUserVoteUpdated(Vote vote) {
        userVote = vote.getValue();
        notifyListener();
    }

    @Override
    public void onUserVoteCast() {
        // do nothing
    }

    @Override
    public void onVideoReceived(Video video) {
        mVideo = video;
        mVoteDAO.getVoteCountsForVideo(mVideo);
        mVoteDAO.getUserVoteForVideo(mVideo);
        mShareDAO.getShareCount(mVideo);
        mShareDAO.getUserShareState(mVideo);
        isVideoLoaded = true;
    }

    @Override
    public void onUserHasShared() {
        hasShared = true;
    }

    @Override
    public void onShareCountUpdated(int count) {
        mShareCount = count;
        isShareLoaded = true;
        notifyListener();
    }
}
