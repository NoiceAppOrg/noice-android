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

    public void setVideo(Video video) {
        mVideo = video;
    }

    public int getUserVote() {
        return userVote;
    }

    public void setUserVote(int userVote) {
        this.userVote = userVote;
    }

    public boolean hasShared() {
        return hasShared;
    }

    public void setShared(boolean hasShared) {
        this.hasShared = hasShared;
        mShareDAO.updateOrCreateShare(mVideo);
    }

    public int getPositiveVoteCount() {
        return mPositiveVoteCount;
    }

    public void setPositiveVoteCount(int positiveVoteCount) {
        mPositiveVoteCount = positiveVoteCount;
    }

    public int getNegativeVoteCount() {
        return mNegativeVoteCount;
    }

    public void setNegativeVoteCount(int negativeVoteCount) {
        mNegativeVoteCount = negativeVoteCount;
    }

    public int getShareCount() {
        return mShareCount;
    }

    public void setShareCount(int shareCount) {
        mShareCount = shareCount;
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
        userVote = Vote.VOTE_NONE;
        mVideoDAO.getMostRecentInBackground();
        setLoading();
    }

    public void getRandomVideo() {
        userVote = Vote.VOTE_NONE;
        mVideoDAO.getRandomInBackground();
        setLoading();
    }

    public void likeVideo() {
        if (mVideo != null) {
            userVote = Vote.VOTE_POSITIVE;
            mVoteDAO.updateOrCreateVote(mVideo, 1);
            notifyListenerIfDoneLoading();
        }
    }

    public void dislikeVideo() {
        if (mVideo != null) {
            userVote = Vote.VOTE_NEGATIVE;
            mVoteDAO.updateOrCreateVote(mVideo, -1);
            notifyListenerIfDoneLoading();
        }
    }

    private void notifyListenerIfDoneLoading() {
        if (isLoaded()) {
            listener.onVideoLoaded(this);
        }
    }

    private void setLoading() {
        isVideoLoaded = false;
        isVoteLoaded = false;
        isShareLoaded = false;
    }

    public boolean isLoaded() {
        return isVideoLoaded && isVoteLoaded && isShareLoaded;
    }

    @Override
    public void onVoteCountsUpdated(int positiveCount, int negativeCount) {
        mPositiveVoteCount = positiveCount;
        mNegativeVoteCount = negativeCount;
        isVoteLoaded = true;
        notifyListenerIfDoneLoading();
    }

    @Override
    public void onUserVoteUpdated(Vote vote) {
        userVote = vote.getValue();
        notifyListenerIfDoneLoading();
    }

    @Override
    public void onUserVoteCast() {
        mVoteDAO.updateVoteCounts(mVideo);
        notifyListenerIfDoneLoading();
    }

    @Override
    public void onVideoReceived(Video video) {
        mVideo = video;
        mVoteDAO.updateVoteCounts(mVideo);
        mShareDAO.updateShareCount(mVideo);
        isVideoLoaded = true;
    }

    @Override
    public void onUserHasShared() {
        if (!hasShared) {
            mShareCount++;
            hasShared = true;
        }
        notifyListenerIfDoneLoading();
    }

    @Override
    public void onShareCountUpdated(int count) {
        mShareCount = count;
        isShareLoaded = true;
        notifyListenerIfDoneLoading();
    }
}
