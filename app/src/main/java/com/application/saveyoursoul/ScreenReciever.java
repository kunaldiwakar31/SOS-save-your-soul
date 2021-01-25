package com.application.saveyoursoul;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

class ScreenReceiver extends BroadcastReceiver {
    int count=0;
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            count++;
            Log.i("SCREEN", "Screen is OFF");
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            count++;
            Log.i("SCREEN","Screen is ON");
        }
        if(count==4){
            MainActivity.sendMessage();
        }
    }
}
