package com.noice.noice.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Video")
public class Video extends ParseObject {

    public int getIndex() {
        return (int) get("index");
    }

    public String getUri() {
        return (String) get("url");
    }

    public String getTitle() {
        return (String) get("title");
    }

    public String getDescription() {
        return (String) get("description");
    }

}
