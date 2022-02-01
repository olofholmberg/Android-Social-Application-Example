package com.example.tddd80application.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.Response
import com.example.tddd80application.R
import com.example.tddd80application.network.*
import com.example.tddd80application.responsedata.ApiMessage
import com.example.tddd80application.responsedata.User

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext

    private val _currentUser: MutableLiveData<User> = MutableLiveData()
    val currentUser: LiveData<User>
        get() = _currentUser

    private val _logoutMessage: MutableLiveData<ApiMessage> = MutableLiveData()
    val logoutMessage: LiveData<ApiMessage>
        get() = _logoutMessage

    private val _apiErrorMessage: MutableLiveData<ApiMessage> = MutableLiveData()
    val apiErrorMessage: LiveData<ApiMessage>
        get() = _apiErrorMessage

    fun fetchCurrentUser(accessToken: String) {

        var fetchQuestionsListener = Response.Listener<User> {
            response ->
            _currentUser.value = response
        }

        var fetchQuestionsErrorListener = Response.ErrorListener{
            error ->
            if (hasApiErrorMessage(error)) {
                _apiErrorMessage.value = getApiErrorMessage(error)
            } else {
                _apiErrorMessage.value = ApiMessage(getDefaultErrorMessage(error))
            }
        }

        var headers = HashMap<String, String>()
        headers["Authorization"] = accessToken

        var fetchCurrentUserGsonRequest = GsonGetRequest<User>(context?.getString(R.string.base_url) + "users/current", User::class.java, headers, fetchQuestionsListener, fetchQuestionsErrorListener)

        if (context != null) VolleySingleton.getInstance(context).addToRequestQueue(fetchCurrentUserGsonRequest)
    }

    fun logout(accessToken: String) {

        var logoutListener = Response.Listener<ApiMessage> {
            response ->
            _logoutMessage.value = response
        }

        var logoutErrorListener = Response.ErrorListener{
            error ->
            if (hasApiErrorMessage(error)) {
                _apiErrorMessage.value = getApiErrorMessage(error)
            } else {
                _apiErrorMessage.value = ApiMessage(getDefaultErrorMessage(error))
            }
        }

        var headers = HashMap<String, String>()
        headers["Authorization"] = accessToken

        var logoutGsonRequest = GsonPostRequest<ApiMessage>(context?.getString(R.string.base_url) + "logout", null, ApiMessage::class.java, headers, logoutListener, logoutErrorListener)

        if (context != null) VolleySingleton.getInstance(context).addToRequestQueue(logoutGsonRequest)
    }

}