package com.qoohoosen.recorder;

import java.io.File;

/**
 * {@code Pcm} is recorder for recording audio in wav format.
 */
final class Pcm extends AbstractRecorder {
    public Pcm(PullTransport pullTransport, File file) {
        super(pullTransport, file);
    }
}