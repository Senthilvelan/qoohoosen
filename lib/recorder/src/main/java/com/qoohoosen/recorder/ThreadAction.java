package com.qoohoosen.recorder;

/**
 * A {@code ThreadAction} is an action which going to be executed on the implementer thread.
 */
interface ThreadAction {
    /**
     * Execute {@code runnable} action on implementer {@code Thread}
     */
    void execute(Runnable action);
}

