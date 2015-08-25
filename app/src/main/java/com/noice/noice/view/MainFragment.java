package com.noice.noice.view;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.noice.noice.R;
import com.noice.noice.model.Video;
import com.noice.noice.model.Vote;
import com.noice.noice.view.model.VideoViewModel;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainFragment extends Fragment implements VideoViewModel.VideoViewModelListener {

    private static final int SHARE_RESULT_CODE = 101;

    @InjectView(R.id.main_content_layout)
    RelativeLayout mContentLayout;
    @InjectView(R.id.main_title_tv)
    TextView titleTextView;
    @InjectView(R.id.main_description_tv)
    TextView descriptionTextView;
    @InjectView(R.id.main_positive_count)
    TextView positiveCountTextView;
    @InjectView(R.id.main_negative_count)
    TextView negativeCountTextView;
    @InjectView(R.id.main_blab_count)
    TextView shareCountTextView;
    @InjectView(R.id.main_positive_button)
    ImageButton positiveButton;
    @InjectView(R.id.main_negative_button)
    ImageButton negativeButton;
    @InjectView(R.id.main_share_button)
    ImageButton shareButton;
    @InjectView(R.id.main_watch_another_button)
    Button watchAnotherButton;

    // video player fragment
    VideoPlayerFragment videoPlayerFragment;

    VideoViewModel mVideoViewModel = new VideoViewModel();


    public MainFragment() {
        // Required empty public constructor
    }

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mVideoViewModel.addListener(this);
        mVideoViewModel.getTodaysVideo();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.inject(this, rootView);
        initViews();
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mVideoViewModel.removeListener();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mContentLayout.setVisibility(View.GONE);
        } else {
            mContentLayout.setVisibility(View.VISIBLE);
        }
    }

    private void initViews() {
        videoPlayerFragment = (VideoPlayerFragment) getChildFragmentManager().findFragmentById(R
                .id.main_video_view);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likeVideo();
            }
        });
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dislikeVideo();
            }
        });
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareLink(mVideoViewModel.getVideo());
            }
        });
        watchAnotherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVideoViewModel.getRandomVideo();
            }
        });
    }

    private void likeVideo() {
        mVideoViewModel.likeVideo();
    }

    private void dislikeVideo() {
        mVideoViewModel.dislikeVideo();
    }

    private void updateVideoViews() {
        if (mVideoViewModel.getVideo().equals(videoPlayerFragment.getVideo())) {
            return;
        }
        videoPlayerFragment.setVideo(mVideoViewModel.getVideo());
        videoPlayerFragment.startVideo();

        // update views
        titleTextView.setText(mVideoViewModel.getVideo().getTitle());
        descriptionTextView.setText(mVideoViewModel.getVideo().getDescription());
    }

    private void updateShareState() {
        if (mVideoViewModel.hasShared()) {
            shareButton.setBackgroundResource(R.drawable.ic_blab_selected);
            shareCountTextView.setTextColor(getResources().getColor(R.color
                    .material_deep_teal_500));
        } else {
            shareButton.setBackgroundResource(R.drawable.ic_blab_unselected);
            shareCountTextView.setTextColor(getResources().getColor(R.color.black));
        }
        shareCountTextView.setText(getResources().getQuantityString(R.plurals.share_count,
                mVideoViewModel.getShareCount(), mVideoViewModel.getShareCount()));
    }

    private void updateLikeState() {
        // update counts for likes/dislikes
        updateVoteCountViews();

        // set defaults
        positiveCountTextView.setTextColor(getResources().getColor(R.color.black));
        negativeCountTextView.setTextColor(getResources().getColor(R.color.black));

        // set share button and text invisible by default
        shareButton.setVisibility(View.GONE);
        shareCountTextView.setVisibility(View.GONE);

        // update views
        if (mVideoViewModel.getUserVote() == Vote.VOTE_POSITIVE) {
            positiveCountTextView.setTextColor(getResources().getColor(R.color.orange));
            positiveButton.setBackgroundResource(R.drawable.ic_haha_selected);
            negativeButton.setBackgroundResource(R.drawable.ic_meh_unselected);
            shareButton.setVisibility(View.VISIBLE);
            shareCountTextView.setVisibility(View.VISIBLE);
        } else if (mVideoViewModel.getUserVote() == Vote.VOTE_NEGATIVE) {
            negativeCountTextView.setTextColor(getResources().getColor(R.color.red));
            positiveButton.setBackgroundResource(R.drawable.ic_haha_unselected);
            negativeButton.setBackgroundResource(R.drawable.ic_meh_selected);
        } else {
            positiveButton.setBackgroundResource(R.drawable.ic_haha_unselected);
            negativeButton.setBackgroundResource(R.drawable.ic_meh_unselected);
        }
    }

    private void updateVoteCountViews() {
        positiveCountTextView.setText(getResources().getQuantityString(R.plurals.haha_count,
                mVideoViewModel.getNegativeVoteCount(), mVideoViewModel.getPositiveVoteCount()));
        negativeCountTextView.setText(getResources().getQuantityString(R.plurals.meh_count,
                mVideoViewModel.getPositiveVoteCount(), mVideoViewModel.getNegativeVoteCount()));
    }

    private void shareLink(Video video) {
        // ignore the click if we don't have anything to share
        //TODO provide more feedback
        if (video == null || TextUtils.isEmpty(video.getUri())) {
            return;
        }

        // create share intent
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.someone_shared));
        sendIntent.putExtra(Intent.EXTRA_TEXT, String.format(getResources().getString(R.string
                .check_out_this_video), video.getUri()));
        sendIntent.setType("text/plain");
        startActivityForResult(sendIntent, SHARE_RESULT_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SHARE_RESULT_CODE) {
            mVideoViewModel.setShared(true);
        }
    }

    @Override
    public void onVideoLoaded(VideoViewModel videoViewModel) {
        mVideoViewModel = videoViewModel;
        updateVideoViews();
        updateLikeState();
        updateVoteCountViews();
        updateShareState();
    }
}
