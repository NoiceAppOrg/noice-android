package com.noice.noice.model;

import android.support.annotation.IntDef;

import com.parse.ParseACL;
import com.parse.ParseClassName;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Vote")
public class Vote extends ParseObject {

    @IntDef({VOTE_NONE, VOTE_POSITIVE, VOTE_NEGATIVE})
    public @interface VoteType {
    }

    public static final int VOTE_NONE = 0;
    public static final int VOTE_POSITIVE = 1;
    public static final int VOTE_NEGATIVE = -1;

    public static ParseACL createVoteACL() {
        ParseACL parseACL = new ParseACL();
        parseACL.setPublicWriteAccess(false);
        parseACL.setPublicReadAccess(true);
        parseACL.setWriteAccess(ParseUser.getCurrentUser(), true);
        return parseACL;
    }

    public ParseInstallation getInstallation() {
        return (ParseInstallation) get("user");
    }

    public Video getVideo() {
        return (Video) get("video");
    }

    public void setVideo(Video video) {
        put("video", video);
    }

    @VoteType
    public int getValue() {
        return ((Number) get("value")).intValue();
    }

    public void setValue(@VoteType int value) {
        put("value", value);
    }

    public void setCurrentInstallation() {
        put("user", ParseInstallation.getCurrentInstallation());
    }

    public boolean isPositive() {
        return getValue() == VOTE_POSITIVE;
    }

    public boolean isNegative() {
        return getValue() == VOTE_NEGATIVE;
    }

    public boolean isNeutral() {
        return getValue() == VOTE_NONE;
    }
}
