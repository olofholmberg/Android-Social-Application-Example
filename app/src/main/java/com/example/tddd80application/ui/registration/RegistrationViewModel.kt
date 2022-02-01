package com.example.tddd80application.ui.registration

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.Response
import com.example.tddd80application.R
import com.example.tddd80application.network.*
import com.example.tddd80application.requestdata.RequestRegistrationData
import com.example.tddd80application.responsedata.ApiMessage
import com.google.gson.Gson

class RegistrationViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext

    private val _registrationMessage: MutableLiveData<ApiMessage> = MutableLiveData()
    val registrationMessage: LiveData<ApiMessage>
        get() = _registrationMessage

    private val _apiErrorMessage: MutableLiveData<ApiMessage> = MutableLiveData()
    val apiErrorMessage: LiveData<ApiMessage>
        get() = _apiErrorMessage

    fun register(username: String, email: String, password: String) {

        var registrationObject = RequestRegistrationData(username, email, password)
        var registrationJsonBody = Gson().toJson(registrationObject)

        var registrationListener = Response.Listener<ApiMessage> {
            response ->
            _registrationMessage.value = response
        }

        var registrationErrorListener = Response.ErrorListener{
            error ->
            if (hasApiErrorMessage(error)) {
                _apiErrorMessage.value = getApiErrorMessage(error)
            } else {
                _apiErrorMessage.value = ApiMessage(getDefaultErrorMessage(error))
            }
        }

        var registrationGsonRequest = GsonPostRequest<ApiMessage>(context?.getString(R.string.base_url) + "users", registrationJsonBody, ApiMessage::class.java, null, registrationListener, registrationErrorListener)

        if (context != null) VolleySingleton.getInstance(context).addToRequestQueue(registrationGsonRequest)
    }

}