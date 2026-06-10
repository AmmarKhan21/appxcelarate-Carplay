package com.car.play.android.app.dialogs

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import com.car.play.android.app.databinding.ExitBinding


object ExitDialog {
    fun exitDialog(context: Activity){
        val dialog = Dialog(context)
        val binding = ExitBinding.inflate(context.layoutInflater)
        dialog.setContentView(binding.root)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding.btnNo.setOnClickListener { dialog.dismiss() }
        binding.btnYes.setOnClickListener { context.finishAffinity() }
        dialog.show()
    }
}