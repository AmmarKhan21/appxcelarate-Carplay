package com.car.android.app.carplay.carconnect.interfaces

interface SavedItemClickEvents {
    fun onSavedClick(id : Int,
        serviceImage: Int, txtServiceName: String, txtDate: String, txtMileage: String,
        txtTotal: String, txtLabour: String, txtParts: String, edtVendorCodes : String,
        edtComment : String
    )
}