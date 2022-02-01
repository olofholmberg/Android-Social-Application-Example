package com.example.tddd80application.ui.users

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.Response
import com.example.tddd80application.R
import com.example.tddd80application.network.*
import com.example.tddd80application.responsedata.ApiMessage
import com.example.tddd80application.responsedata.OtherUsers

class OtherUsersViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext

    private val _otherUsers: MutableLiveData<OtherUsers> = MutableLiveData()
    val otherUsers: LiveData<OtherUsers>
        get() = _otherUsers

    private val _apiErrorMessage: MutableLiveData<ApiMessage> = MutableLiveData()
    val apiErrorMessage: LiveData<ApiMessage>
        get() = _apiErrorMessage

    fun fetchOtherUsers(accessToken: String) {

        var fetchOtherUsersListener = Response.Listener<OtherUsers> {
            response ->
            _otherUsers.value = response
        }

        var fetchOtherUsersErrorListener = Response.ErrorListener {
            error ->
            if (hasApiErrorMessage(error)) {
                _apiErrorMessage.value = getApiErrorMessage(error)
            } else {
                _apiErrorMessage.value = ApiMessage(getDefaultErrorMessage(error))
            }
        }

        var headers = HashMap<String, String>()
        headers["Authorization"] = accessToken

        var fetchOtherUsersGsonRequest = GsonGetRequest<OtherUsers>(context?.getString(R.string.base_url) + "users", OtherUsers::class.java, headers, fetchOtherUsersListener, fetchOtherUsersErrorListener)

        if (context != null) VolleySingleton.getInstance(context).addToRequestQueue(fetchOtherUsersGsonRequest)
    }

}