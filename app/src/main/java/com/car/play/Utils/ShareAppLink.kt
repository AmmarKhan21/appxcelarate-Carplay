package com.car.play.android.app.Utils

import android.content.Context
import android.content.Intent


object ShareAppLink {

    fun shareApp(context: Context){
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My application name")
            var shareMessage = "\nLet me recommend you this application\n\n"
            shareMessage += "https://play.google.com/store/apps/details?id=${context.packageName}\n\n"
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            context.startActivity(Intent.createChooser(shareIntent, "Choose one"))
        } catch (e: Exception) {
            // Handle exception
        }
    }
}