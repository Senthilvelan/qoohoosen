package com.qoohoosen.recorder;

import java.io.IOException;

/**
 * A Recorder who can start and stop recording with startRecording() and stopRecording() method
 * respectively.
 */
public interface Recorder {
    void startRecording();

    void stopRecording() throws IOException;

    void pauseRecording();

    void resumeRecording();

    /**
     * Interface definition for a callback to be invoked when a silence is measured.
     */
    interface OnSilenceListener {
        /**
         * Called when a silence measured
         *
         * @param silenceTime The silence measured
         */
        void onSilence(long silenceTime);
    }
}
