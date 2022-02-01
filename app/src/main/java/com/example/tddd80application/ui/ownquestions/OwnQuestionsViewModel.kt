package com.example.tddd80application.ui.ownquestions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.Response
import com.example.tddd80application.R
import com.example.tddd80application.network.*
import com.example.tddd80application.responsedata.ApiMessage
import com.example.tddd80application.responsedata.Questions

class OwnQuestionsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext

    private val _ownQuestions: MutableLiveData<Questions> = MutableLiveData()
    val ownQuestions: LiveData<Questions>
        get() = _ownQuestions

    private val _apiErrorMessage: MutableLiveData<ApiMessage> = MutableLiveData()
    val apiErrorMessage: LiveData<ApiMessage>
        get() = _apiErrorMessage

    fun fetchOwnQuestions(accessToken: String) {

        var fetchOwnQuestionsListener = Response.Listener<Questions> {
            response ->
            _ownQuestions.value = response
        }

        var fetchOwnQuestionsErrorListener = Response.ErrorListener{
            error ->
            if (hasApiErrorMessage(error)) {
                _apiErrorMessage.value = getApiErrorMessage(error)
            } else {
                _apiErrorMessage.value = ApiMessage(getDefaultErrorMessage(error))
            }
        }

        var headers = HashMap<String, String>()
        headers["Authorization"] = accessToken

        var fetchOwnQuestionsGsonRequest = GsonGetRequest<Questions>(context?.getString(R.string.base_url) + "myquestions", Questions::class.java, headers, fetchOwnQuestionsListener, fetchOwnQuestionsErrorListener)

        if (context != null) VolleySingleton.getInstance(context).addToRequestQueue(fetchOwnQuestionsGsonRequest)
    }

}