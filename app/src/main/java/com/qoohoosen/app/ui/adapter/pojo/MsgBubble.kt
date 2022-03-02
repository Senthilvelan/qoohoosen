package com.qoohoosen.app.ui.adapter.pojo

class MsgBubble {
    @JvmField
    var text: String? = null

    @JvmField
    var path: String? = null

    @JvmField
    var type: Int

    @JvmField
    var time = 0

    @JvmField
    var index = 0

    constructor(text: String?) {
        this.text = text
        type = TYPE_TEXT
    }

    constructor(index: Int, time: Int, path: String?) {
        this.index = index
        this.time = time
        this.path = path
        type = TYPE_AUDIO
    }


    companion object {
        @JvmField
        var TYPE_TEXT = 1

        @JvmField
        var TYPE_AUDIO = 21
    }
}