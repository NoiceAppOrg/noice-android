package com.noice.noice.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.noice.noice.model.Video;
import com.noice.noice.util.KeyHelper;
import com.noice.noice.util.URIHelper;

public class VideoPlayerFragment extends YouTubePlayerSupportFragment implements YouTubePlayer.OnInitializedListener {

    private static final String TAG = "VideoPlayerFragment";

    private String youTubeVideoId;
    private YouTubePlayer youTubePlayer;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        initialize(KeyHelper.getYouTubePlayerKey(getActivity()), this);
    }

    /**
     * Set data for the video that we will be playing. Attempts to parse the YouTube video id from the video's uri
     *
     * @param video
     */
    public void setVideo(Video video) {
        if (video != null) {
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
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
        if (!wasRestored) {
            youTubePlayer = player;
            startVideo();
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        // do nothing
    }
}
