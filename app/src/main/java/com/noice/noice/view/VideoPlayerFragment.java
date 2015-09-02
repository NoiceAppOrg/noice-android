package com.noice.noice.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.noice.noice.metrics.TrackerConstants;
import com.noice.noice.model.Video;
import com.noice.noice.util.KeyHelper;
import com.noice.noice.util.URIHelper;
import com.parse.ParseAnalytics;

public class VideoPlayerFragment extends YouTubePlayerSupportFragment implements YouTubePlayer
        .OnInitializedListener, YouTubePlayer.PlaybackEventListener, YouTubePlayer
        .PlayerStateChangeListener {

    private static final String TAG = "VideoPlayerFragment";

    private String youTubeVideoId;
    private YouTubePlayer youTubePlayer;
    private Video mVideo;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        initialize(KeyHelper.getYouTubePlayerKey(getActivity()), this);
    }

    public Video getVideo() {
        return mVideo;
    }

    /**
     * Set data for the video that we will be playing. Attempts to parse the YouTube video id
     * from the video's uri
     *
     * @param video
     */
    public void setVideo(Video video) {
        if (video != null) {
            mVideo = video;
            try {
                youTubeVideoId = URIHelper.extractYoutubeId(video.getUri());
            } catch (Exception e) {
                Log.e(TAG, "Can't get id from YouTube video");
            }
        }
    }

    /**
     * Cue the video for playback if the player has loaded and we have a YouTube video id
     */
    public void startVideo() {
        if (youTubePlayer != null & !TextUtils.isEmpty(youTubeVideoId)) {
            youTubePlayer.cueVideo(youTubeVideoId);
        }
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
                                        boolean wasRestored) {
        youTubePlayer = player;
        youTubePlayer.setPlaybackEventListener(this);
        youTubePlayer.setPlayerStateChangeListener(this);
        startVideo();
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider,
                                        YouTubeInitializationResult youTubeInitializationResult) {
        // do nothing
    }

    @Override
    public void onPlaying() {
        ParseAnalytics.trackEventInBackground(TrackerConstants.PLAY_ACTION);
    }

    @Override
    public void onPaused() {
        ParseAnalytics.trackEventInBackground(TrackerConstants.PAUSE_ACTION);
    }

    @Override
    public void onStopped() {
        // do nothing
    }

    @Override
    public void onBuffering(boolean b) {
        // do nothing
    }

    @Override
    public void onSeekTo(int i) {
        // do nothing
    }

    @Override
    public void onLoading() {
        // do nothing
    }

    @Override
    public void onLoaded(String s) {
        // do nothing
    }

    @Override
    public void onAdStarted() {
        // do nothing
    }

    @Override
    public void onVideoStarted() {
        // do nothing
    }

    @Override
    public void onVideoEnded() {
        ParseAnalytics.trackEventInBackground(TrackerConstants.VIDEO_ENDED);
    }

    @Override
    public void onError(YouTubePlayer.ErrorReason errorReason) {
        // do nothing
    }
}
