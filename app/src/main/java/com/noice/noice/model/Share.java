package com.noice.noice.model;

import com.parse.ParseClassName;
import com.parse.ParseInstallation;
import com.parse.ParseObject;

@ParseClassName("Share")
public class Share extends ParseObject{

    public ParseInstallation getInstallation() {
        return (ParseInstallation) get("user");
    }

    public Video getVideo() {
        return (Video) get("video");
    }

    public void setCurrentInstallation() {
        put("user", ParseInstallation.getCurrentInstallation());
    }

    public void setVideo(Video video) {
        put("video", video);
    }
}
