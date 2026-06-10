package com.car.play.android.app.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.car.play.android.app.R
import com.car.play.android.app.Utils.CustomToast.Companion.showToast
import com.car.play.android.app.Utils.RateUs.rateUs
import com.car.play.android.app.databinding.RateUsDialogBinding


object RatingDialog {
    fun ratingDialog(fragment: Fragment,context: Context) {
        val dialog = Dialog(fragment.requireContext())
        val binding = RateUsDialogBinding.inflate(fragment.layoutInflater)
        dialog.setContentView(binding.root)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.lvCancel.setOnClickListener { dialog.dismiss() }
        binding.lvSubmit.setOnClickListener {
            val rating = binding.ratingBar.rating  // Get the rating from the RatingBar
            if (rating == 0f) {
                showToast(context,context.getString(R.string.please_provide_rating))
            } else {
                if (rating < 3) {
//                context.startActivity(Intent(context, FeedbackActivity::class.java))
                    showToast(context, context.getString(R.string.submit))
                    dialog.dismiss()
                } else {
                    showToast(context, context.getString(R.string.submit))
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }

}
