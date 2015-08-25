package com.noice.noice.model;

import com.parse.ParseACL;
import com.parse.ParseClassName;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Share")
public class Share extends ParseObject {

    public static ParseACL createShareACL() {
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

    public void setCurrentInstallation() {
        put("user", ParseInstallation.getCurrentInstallation());
    }
}
