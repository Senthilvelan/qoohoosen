package com.qoohoosen.recorder;

import java.io.File;

/**
 * Essential APIs for working with Recorder.
 */
public final class OmRecorder {
    private OmRecorder() {
    }

    public static Recorder pcm(PullTransport pullTransport, File file) {
        return new Pcm(pullTransport, file);
    }

    public static Recorder wav(PullTransport pullTransport, File file) {
        return new Wav(pullTransport, file);
    }
}
