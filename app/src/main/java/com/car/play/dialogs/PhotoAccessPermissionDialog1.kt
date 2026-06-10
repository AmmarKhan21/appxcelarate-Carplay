package com.app.lock.hide.apps.secure.utils.dialogs

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup
import android.view.Window
import com.car.play.android.app.R
import com.car.play.android.app.databinding.DialogAccessFile1Binding


class PhotoAccessPermissionDialog1(private val mContext: Context) : Dialog(mContext) {

    private lateinit var binding: DialogAccessFile1Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDialog()
        setupViews()
        setupListeners()
    }

    private fun setupDialog() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding = DialogAccessFile1Binding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setupViews() {
        val layoutParams = binding.root.layoutParams as ViewGroup.MarginLayoutParams
        val margin = context.resources.getDimensionPixelSize(R.dimen.margin_dialog)
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParams.setMargins(margin, 0, margin, 0)
        binding.root.layoutParams = layoutParams
    }

    private fun setupListeners() {
        binding.txtDeny.setOnClickListener {
            dismiss()
        }

        binding.txtAllow.setOnClickListener {
            navigateToAppSettings()
            dismiss()
        }
    }

    private fun navigateToAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", mContext.packageName, null)
        }
        mContext.startActivity(intent)
    }
}