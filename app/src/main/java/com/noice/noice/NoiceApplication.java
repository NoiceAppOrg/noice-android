package com.noice.noice;

import android.app.Application;
import android.util.Log;

import com.noice.noice.model.Share;
import com.noice.noice.model.Video;
import com.noice.noice.model.Vote;
import com.noice.noice.util.KeyHelper;
import com.parse.Parse;
import com.parse.ParseCrashReporting;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class NoiceApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        // Enable Crash Reporting
        ParseCrashReporting.enable(this);

        Parse.initialize(this, KeyHelper.getParseApplicationId(this), KeyHelper.getParseClientKey
                (this));
        ParseObject.registerSubclass(Video.class);
        ParseObject.registerSubclass(Vote.class);
        ParseObject.registerSubclass(Share.class);
        ParseInstallation.getCurrentInstallation().saveInBackground();
        ParseUser.enableAutomaticUser();

        // setup push message listening
        ParsePush.subscribeInBackground("", new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
                } else {
                    Log.e("com.parse.push", "failed to subscribe for push", e);
                }
            }
        });
    }
}
