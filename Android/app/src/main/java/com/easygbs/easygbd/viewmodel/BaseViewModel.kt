package com.easygbs.easygbd.viewmodel;
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

open class BaseViewModel(application:Application): AndroidViewModel( application) {
    var BaseViewModelTAG = BaseViewModel::class.java.getSimpleName()

}