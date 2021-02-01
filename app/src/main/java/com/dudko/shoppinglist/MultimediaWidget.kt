package com.dudko.shoppinglist

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.widget.RemoteViews
import android.widget.Toast

val photos = arrayOf(R.drawable.u21, R.drawable.u22)
val songs = arrayOf(R.raw.onetree, R.raw.beautiful)
val descriptions = arrayOf("Playing U2: One Tree Hill", "Playing U2: Beautiful Day")

var currPhotoId = 0
var currSongId = 0

var mediaPlayer: MediaPlayer? = null

/**
 * Implementation of App Widget functionality.
 */
class MultimediaWidget : AppWidgetProvider() {

    private var requestCode = 0

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, requestCode)
            requestCode += 5
        }
    }

    override fun onEnabled(context: Context) {
        println("Enabled")
    }

    override fun onDisabled(context: Context) {
        println("Disabled")
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val views = RemoteViews(context?.packageName, R.layout.multimedia_widget)
        val action = intent?.getStringExtra("action");
        if (action.equals("next_photo")) {
            currPhotoId = (currPhotoId + 1) % photos.size
            views.setImageViewResource(R.id.imageView, photos[currPhotoId])
        } else if (action.equals(("play_pause"))) {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(context, songs[currSongId])
                views.setTextViewText(R.id.text_view, descriptions[currSongId])
            }

            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            } else {
                mediaPlayer?.start()
            }
        } else if (action.equals("stop")) {
            mediaPlayer?.stop()
            mediaPlayer = MediaPlayer.create(context, songs[currSongId])

        } else if (action.equals("next_song")) {
            mediaPlayer?.stop()
            currSongId = (currSongId + 1) % songs.size
            mediaPlayer = MediaPlayer.create(context, songs[currSongId])
            views.setTextViewText(R.id.text_view, descriptions[currSongId])
            mediaPlayer?.start()
        }

        if (context != null) {
            AppWidgetManager.getInstance(context).updateAppWidget(ComponentName(context, MultimediaWidget::class.java), views)
        }
        super.onReceive(context, intent)
    }
}

internal fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        requestCode: Int
) {
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.multimedia_widget)

    val nextPhotoIntent = Intent()
    nextPhotoIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
    // for some reason it was not picking up my custom actions, so we're using extras
    nextPhotoIntent.putExtra("action", "next_photo")
    val nextPhotoPendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            nextPhotoIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
    )
    views.setOnClickPendingIntent(R.id.next_photo, nextPhotoPendingIntent)

    val playPauseIntent = Intent()
    playPauseIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
    playPauseIntent.putExtra("action", "play_pause")
    val playPausePendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode + 1,
            playPauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
    )
    views.setOnClickPendingIntent(R.id.play_pause, playPausePendingIntent)


    val stopIntent = Intent()
    stopIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
    stopIntent.putExtra("action", "stop")
    val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode + 2,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
    )
    views.setOnClickPendingIntent(R.id.stop, stopPendingIntent)


    val nextSongIntent = Intent()
    nextSongIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
    nextSongIntent.putExtra("action", "next_song")
    val nextSongPendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode + 3,
            nextSongIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
    )
    views.setOnClickPendingIntent(R.id.next_song, nextSongPendingIntent)


    views.setImageViewResource(R.id.imageView, R.drawable.u21)
    views.setOnClickPendingIntent(R.id.imageView, nextPhotoPendingIntent)

    val goToWebsiteIntent = Intent(Intent.ACTION_VIEW)
    goToWebsiteIntent.data = Uri.parse("https://www.u2.com")
    val goToWebsitePendingIntent = PendingIntent.getActivity(
            context,
            requestCode + 4,
            goToWebsiteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
    )
    views.setOnClickPendingIntent(R.id.goToWebpage, goToWebsitePendingIntent)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}
