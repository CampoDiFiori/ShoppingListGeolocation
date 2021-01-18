package com.dudko.shoppinglist

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceReceiver : BroadcastReceiver() {

    var id: Int = 0
    var createdNotificationChannel: Boolean = false

    private fun createNotificationChannel(context: Context){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                R.string.channelID.toString(),
                R.string.channel_name.toString(),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.description = R.string.channel_description.toString()
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }


    fun sendNotification(context: Context, contentTitle: String, contentText: String) {
        val intent = Intent(context, FavoriteShopListActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(context, R.string.channelID.toString())
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(id++, notification)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (!createdNotificationChannel) {
            createNotificationChannel(context)
            createdNotificationChannel = true
        }

        val shopName = intent.getStringExtra("name")
        val shopDescription = intent.getStringExtra("description") ?: "Couldn't fetch description"

        val geoEvent = GeofencingEvent.fromIntent(intent)
        val triggering = geoEvent.triggeringGeofences
        for( geo in triggering){
            Log.i("geofence", "Geofence z id: ${geo.requestId} aktywny.")
        }
        if(geoEvent.geofenceTransition ==
            Geofence.GEOFENCE_TRANSITION_ENTER){
            Log.i("geofences", "Entered: ${geoEvent.triggeringLocation.toString()}")
            sendNotification(context, "You entered $shopName", shopDescription)
        }else if(geoEvent.geofenceTransition ==
            Geofence.GEOFENCE_TRANSITION_EXIT){
            sendNotification(context, "You left $shopName", shopDescription)
            Log.i("geofences", "Left: ${geoEvent.triggeringLocation.toString()}")
        }else{
            Log.e("geofences", "Error.")
        }
    }
}