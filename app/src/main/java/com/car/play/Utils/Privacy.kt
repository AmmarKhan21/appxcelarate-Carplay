package com.car.play.android.app.Utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.car.play.android.app.R



object Privacy {
    fun privacyPolicy(context: Context){
        val uri = Uri.parse(context.getString(R.string.privacy))
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    }
}