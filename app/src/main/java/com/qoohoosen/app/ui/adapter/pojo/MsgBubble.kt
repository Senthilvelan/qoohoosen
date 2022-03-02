package com.qoohoosen.app.ui.adapter.pojo

import com.qoohoosen.app.ui.adapter.pojo.MsgBubble

class MsgBubble {
    @JvmField
    var text: String? = null
    @JvmField
    var type: Int
    @JvmField
    var time = 0

    constructor(text: String?) {
        this.text = text
        type = TYPE_TEXT
    }

    constructor(time: Int) {
        this.time = time
        type = TYPE_AUDIO
    }

    companion object {
        @JvmField
        var TYPE_TEXT = 1
        @JvmField
        var TYPE_AUDIO = 21
    }
}