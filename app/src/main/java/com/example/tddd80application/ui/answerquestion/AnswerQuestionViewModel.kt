package com.example.tddd80application.ui.answerquestion

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.Response
import com.example.tddd80application.R
import com.example.tddd80application.network.*
import com.example.tddd80application.requestdata.RequestAnswerData
import com.example.tddd80application.responsedata.ApiMessage
import com.google.gson.Gson

class AnswerQuestionViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext

    private val _answerQuestionMessage: MutableLiveData<ApiMessage> = MutableLiveData()
    val answerQuestionMessage: LiveData<ApiMessage>
        get() = _answerQuestionMessage

    private val _apiErrorMessage: MutableLiveData<ApiMessage> = MutableLiveData()
    val apiErrorMessage: LiveData<ApiMessage>
        get() = _apiErrorMessage

    fun postAnswer(accessToken: String, answerBody: String, questionId: String) {

        val requestAnswerData = RequestAnswerData(answerBody)
        val postAnswerJsonBody = Gson().toJson(requestAnswerData)

        val postAnswerListener = Response.Listener<ApiMessage> {
            response ->
            _answerQuestionMessage.value = response
        }

        val postAnswerErrorListener = Response.ErrorListener{
            error ->
            if (hasApiErrorMessage(error)) {
                _apiErrorMessage.value = getApiErrorMessage(error)
            } else {
                _apiErrorMessage.value = ApiMessage(getDefaultErrorMessage(error))
            }
        }

        val headers = HashMap<String, String>()
        headers["Authorization"] = accessToken

        val postAnswerGsonRequest = GsonPostRequest<ApiMessage>(context?.getString(R.string.base_url) + "answer_question/" + questionId, postAnswerJsonBody, ApiMessage::class.java, headers, postAnswerListener, postAnswerErrorListener)

        if (context != null) VolleySingleton.getInstance(context).addToRequestQueue(postAnswerGsonRequest)

    }

}