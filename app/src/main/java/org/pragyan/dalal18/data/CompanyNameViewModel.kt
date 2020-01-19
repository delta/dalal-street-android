package org.pragyan.dalal18.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CompanyNameViewModel: ViewModel() {

    val companyName = MutableLiveData<String>()

    fun updateCompanySelectedMarketDepth(company: String) {
        companyName.value = company
    }
}