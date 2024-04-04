package com.ajaha.notes.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val mutableLiveData = MutableLiveData<String>()

    fun set(value: String) {
        mutableLiveData.value = value
    }

    fun get(): MutableLiveData<String> {
        return mutableLiveData
    }
}