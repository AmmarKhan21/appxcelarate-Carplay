/*
package com.car.play.android.app.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import androidx.fragment.app.Fragment


object UnlockDialog {
    fun unlockDialog(context: Fragment, onPurchaseClick: () -> Unit) {
        val dialog = Dialog(context.requireContext())
        val binding = UnlockEmergencyDialogBinding.inflate(context.layoutInflater)
        dialog.setContentView(binding.root)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.icClose.setOnClickListener { dialog.dismiss() }
        binding.tvNoThanks.setOnClickListener { dialog.dismiss() }
        binding.root.setOnClickListener {
            onPurchaseClick()
            dialog.dismiss()
        }

        dialog.show()
    }
}
*/
