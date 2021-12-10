package com.example.b20173043_app.model;

import androidx.lifecycle.MutableLiveData;


public class DMUserLiveData extends MutableLiveData<DMUser> {

    private static DMUserLiveData __instance = null;

    public static DMUserLiveData getInstance() {
        if (__instance == null) {
            __instance = new DMUserLiveData();
        }

        return __instance;
    }
}
