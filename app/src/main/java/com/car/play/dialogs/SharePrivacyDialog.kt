package com.car.play.android.app.dialogs

import android.app.AlertDialog
import android.content.Context
import com.car.play.android.app.R
import com.car.play.android.app.Utils.Privacy
import com.car.play.android.app.Utils.ShareAppLink


object SharePrivacyDialog {

    fun showSharePrivacyDialog(context: Context, action: String) {
        val title = if (action == "share") context.getString(R.string.txt_share_app) else context.getString(
            R.string.txt_open_privacy)
        val message = if (action == "share") context.getString(R.string.txt_share) else context.getString(
            R.string.txt_privacy_open)
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton(context.getString(R.string.txtYes)) { dialog, _ ->
            if (action == "share") {
                ShareAppLink.shareApp(context)
            } else {
                Privacy.privacyPolicy(context)
            }
            dialog.dismiss()
        }
        alertDialogBuilder.setNegativeButton(context.getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}
