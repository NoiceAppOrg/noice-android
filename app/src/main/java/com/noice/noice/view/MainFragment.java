package com.noice.noice.view;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.noice.noice.R;
import com.noice.noice.dao.ShareDAO;
import com.noice.noice.dao.VideoDAO;
import com.noice.noice.dao.VoteDAO;
import com.noice.noice.model.Video;
import com.noice.noice.model.Vote;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainFragment extends Fragment implements VoteDAO.VoteListener, VideoDAO.VideoListener, ShareDAO.ShareListener {

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

    // video player fragment
    VideoPlayerFragment videoPlayerFragment;

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

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        return fragment;
    }

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mVideoDAO.addListener(this);
        mVoteDAO.addListener(this);
        mShareDAO.addListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // query for most recent video
        mVideoDAO.getMostRecentInBackground();
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
        mVideoDAO.removeListener(this);
        mVoteDAO.removeListener(this);
        mShareDAO.removeListener(this);
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
        videoPlayerFragment = (VideoPlayerFragment) getChildFragmentManager().findFragmentById(R.id.main_video_view);
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
                shareLink(mVideo);
            }
        });
    }

    private void likeVideo() {
        if (mVideo != null) {
            userVote = Vote.VOTE_POSITIVE;
            mVoteDAO.updateOrCreateVote(mVideo, 1);
            updateLikeState();
        }
    }

    private void dislikeVideo() {
        if (mVideo != null) {
            userVote = Vote.VOTE_NEGATIVE;
            mVoteDAO.updateOrCreateVote(mVideo, -1);
            updateLikeState();
        }
    }

    private void updateViews() {
        videoPlayerFragment.setVideo(mVideo);
        videoPlayerFragment.startVideo();

        // update views
        titleTextView.setText(mVideo.getTitle());
        descriptionTextView.setText(mVideo.getDescription());
        updateLikeState();
        updateShareState();
    }

    private void updateShareState() {
        if (hasShared) {
            shareButton.setBackgroundResource(R.drawable.ic_blab_selected);
            shareCountTextView.setTextColor(getResources().getColor(R.color.material_deep_teal_500));
        } else {
            shareButton.setBackgroundResource(R.drawable.ic_blab_unselected);
            shareCountTextView.setTextColor(getResources().getColor(R.color.black));
        }
        shareCountTextView.setText(getResources().getQuantityString(R.plurals.share_count, mShareCount, mShareCount));
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
        if (userVote == Vote.VOTE_POSITIVE) {
            positiveCountTextView.setTextColor(getResources().getColor(R.color.orange));
            positiveButton.setBackgroundResource(R.drawable.ic_haha_selected);
            negativeButton.setBackgroundResource(R.drawable.ic_meh_unselected);
            shareButton.setVisibility(View.VISIBLE);
            shareCountTextView.setVisibility(View.VISIBLE);
        } else if (userVote == Vote.VOTE_NEGATIVE) {
            negativeCountTextView.setTextColor(getResources().getColor(R.color.red));
            positiveButton.setBackgroundResource(R.drawable.ic_haha_unselected);
            negativeButton.setBackgroundResource(R.drawable.ic_meh_selected);
        }
    }

    private void updateVoteCountViews() {
        positiveCountTextView.setText(getResources().getQuantityString(R.plurals.haha_count, mPositiveVoteCount, mPositiveVoteCount));
        negativeCountTextView.setText(getResources().getQuantityString(R.plurals.meh_count, mNegativeVoteCount, mNegativeVoteCount));
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
        sendIntent.putExtra(Intent.EXTRA_TEXT, String.format(getResources().getString(R.string.check_out_this_video), video.getUri()));
        sendIntent.setType("text/plain");
        startActivityForResult(sendIntent, SHARE_RESULT_CODE);

        // save share on server
        mShareDAO.updateOrCreateShare(mVideo);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SHARE_RESULT_CODE) {
            hasShared = true;
            updateShareState();
        }
    }

    @Override
    public void onVoteCountsUpdated(int positiveCount, int negativeCount) {
        mPositiveVoteCount = positiveCount;
        mNegativeVoteCount = negativeCount;
        updateVoteCountViews();
    }

    @Override
    public void onUserVoteUpdated(Vote vote) {
        userVote = vote.getValue();
        updateLikeState();
    }

    @Override
    public void onUserVoteCast() {
        mVoteDAO.updateVoteCounts(mVideo);
    }

    @Override
    public void onVideoReceived(Video video) {
        mVideo = video;
        updateViews();
        mVoteDAO.updateVoteCounts(mVideo);
        mShareDAO.updateShareCount(mVideo);
    }

    @Override
    public void onUserHasShared() {
        if (!hasShared) {
            mShareCount++;
            hasShared = true;
        }
        updateShareState();
    }

    @Override
    public void onShareCountUpdated(int count) {
        mShareCount = count;
        updateShareState();
    }
}
