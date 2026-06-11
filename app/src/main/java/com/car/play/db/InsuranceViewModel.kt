package com.car.play.android.app.db

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class InsuranceViewModel(application: Application) : AndroidViewModel(application) {
    private val insuranceDao = AppDatabase.getDatabase(application).insuranceDao()
    val allInsurance: LiveData<List<InsuranceEntity>> = insuranceDao.getAllInsurance()
    val nextExpiring: LiveData<InsuranceEntity?> = insuranceDao.getNextExpiring()

    fun addInsurance(policyNumber: String, provider: String, type: String, premium: Double, startDate: String, endDate: String, agentName: String, agentPhone: String, documentPath: String, notes: String) {
        viewModelScope.launch {
            insuranceDao.insert(InsuranceEntity(policyNumber = policyNumber, provider = provider, type = type, premium = premium, startDate = startDate, endDate = endDate, agentName = agentName, agentPhone = agentPhone, documentPath = documentPath, notes = notes))
        }
    }
    fun deleteInsurance(insurance: InsuranceEntity) { viewModelScope.launch { insuranceDao.delete(insurance) } }
}
