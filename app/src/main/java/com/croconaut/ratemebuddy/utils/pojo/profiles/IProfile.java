package com.croconaut.ratemebuddy.utils.pojo.profiles;


import android.net.Uri;

import com.croconaut.ratemebuddy.data.pojo.Status;

public interface IProfile {
    String getIdent(); //unique ID
    String getName();
    Uri getThumbUri();
    Status getStatus();
    long getTimeStamp();
}
