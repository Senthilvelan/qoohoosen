package com.qoohoosen.utils;

import com.qoohoosen.app.R;

public interface Constable {

    int RECORD_START = R.raw.record_start;
    int RECORD_COMPLETED = R.raw.record_finished;
    int RECORD_CANCEL = R.raw.record_error;

    int MIN_RECORD_TIME_THRESHOLD = 2;
    long DEBOUNCE_INTERVAL_DEFAULT = 900;
    long DEBOUNCE_INTERVAL = 500;

    int TIMER_1000 = 1000;


    int FREQUENCY = 44100;

    String ACTION_STOP_FOREGROUND = "com.qoohoo.app.stop_audio";
    String INTENT_PATH_AUDIO = "path_audio";

    String FILE_EXT = ".wav";


    int NOTI_ID = 123;
    String NOTI_CHANNEL_GROUP_ID = "qoohoo_group_audio";
    String NOTI_CHANNEL_GROUP_NAME = "qoohoo_notification";
    String NOTI_SERVICE_CHANNEL = "qoohoo_service";
    String NOTI_SERVICE = "Qoohoo Notifications";


}
