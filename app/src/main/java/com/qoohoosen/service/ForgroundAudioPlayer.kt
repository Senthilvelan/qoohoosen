package com.qoohoosen.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.qoohoosen.app.R
import com.qoohoosen.app.ui.MainActivity
import com.qoohoosen.utils.Constable.*
import java.io.IOException


class ForgroundAudioPlayer : Service() {
    //Notification for ON-going
    private var iconNotification: Bitmap? = null
    private var notification: Notification? = null
    var mNotificationManager: NotificationManager? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        player = MediaPlayer()

        if (intent?.action != null && intent.action.equals(
                ACTION_STOP_FOREGROUND,
                ignoreCase = true
            )
        ) {
            stopForeground(true)
            stopSelf()
        }

        if (intent != null) {
            val audioPath = intent.getStringExtra(INTENT_PATH_AUDIO)
            playAudio(audioPath)
        }
        return START_STICKY
    }

    private fun playAudio(audioPath: String?) {
        if (player != null && player!!.isPlaying) {
            player!!.release()
        }

        playTinyMusic(this, audioPath)
        generateForegroundNotification()
    }


    private fun generateForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intentMainLanding = Intent(this, MainActivity::class.java)
            val pendingIntent =
                PendingIntent.getActivity(this, 0, intentMainLanding, 0)
            iconNotification = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
            if (mNotificationManager == null) {
                mNotificationManager =
                    this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                assert(mNotificationManager != null)
                mNotificationManager?.createNotificationChannelGroup(
                    NotificationChannelGroup(NOTI_CHANNEL_GROUP_ID, NOTI_CHANNEL_GROUP_NAME)
                )
                val notificationChannel =
                    NotificationChannel(
                        NOTI_SERVICE_CHANNEL, NOTI_SERVICE,
                        NotificationManager.IMPORTANCE_MIN
                    )
                notificationChannel.enableLights(false)
                notificationChannel.lockscreenVisibility = Notification.VISIBILITY_SECRET
                mNotificationManager?.createNotificationChannel(notificationChannel)
            }
            val builder = NotificationCompat
                .Builder(this, NOTI_SERVICE_CHANNEL)

            builder.setContentTitle(
                StringBuilder(resources.getString(R.string.app_name))
                    .append("  Audio playing !")
                    .toString()
            )
                .setTicker(
                    StringBuilder(resources.getString(R.string.app_name))
                        .append("\"  Audio playing !")
                        .toString()
                )
                .setContentText("Open qoohoo")
                .setSmallIcon(R.drawable.ic_qoohoo_logo_white)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setWhen(0)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
            if (iconNotification != null) {
                builder
                    .setLargeIcon(
                        Bitmap
                            .createScaledBitmap(
                                iconNotification!!,
                                128, 128, false
                            )
                    )
            }
            builder.color = resources.getColor(R.color.black_smoke)
            notification = builder.build()
            startForeground(NOTI_ID, notification)
        }

    }

    private var player: MediaPlayer? = null

    @Synchronized
    fun playTinyMusic(context: Context?, path: String?) {
        if (path == null) return
        if (path.length <= 0) return
        try {
            player!!.setDataSource(path)
            player!!.prepare()
            player!!.start()

            player!!.setOnCompletionListener(OnCompletionListener { mp: MediaPlayer? ->
                mp!!.release()
                player = null
                stopForeground(true)
                stopSelf()
            })

            player!!.setOnErrorListener { mp: MediaPlayer?, what: Int, extra: Int ->
                mp!!.release()
                player = null
                stopForeground(true)
                stopSelf()
                false
            }

            player!!.setLooping(false)
        } catch (e: IOException) {
            e.printStackTrace()
        } //eof try...catch
    } //eof playTinyMusic

}