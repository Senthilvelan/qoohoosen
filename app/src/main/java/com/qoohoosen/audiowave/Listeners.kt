package com.qoohoosen.audiowave


interface OnSamplingListener {
    fun onComplete()
}

interface OnProgressListener {
    fun onStartTracking(progress: Float)
    fun onStopTracking(progress: Float)
    fun onProgressChanged(progress: Float, byUser: Boolean)
}