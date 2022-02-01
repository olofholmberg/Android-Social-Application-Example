package com.example.tddd80application.ui.questiondetails

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.Response
import com.example.tddd80application.R
import com.example.tddd80application.network.*
import com.example.tddd80application.responsedata.Answers
import com.example.tddd80application.responsedata.ApiMessage
import com.example.tddd80application.responsedata.Question

class QuestionDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext

    private val _currentQuestion: MutableLiveData<Question> = MutableLiveData()
    val currentQuestion: LiveData<Question>
        get() = _currentQuestion

    private val _answers: MutableLiveData<Answers> = MutableLiveData()
    val answers: LiveData<Answers>
        get() = _answers

    private val _likeQuestionMessage: MutableLiveData<ApiMessage> = MutableLiveData()
    val likeQuestionMessage: LiveData<ApiMessage>
        get() = _likeQuestionMessage

    private val _apiErrorMessage: MutableLiveData<ApiMessage> = MutableLiveData()
    val apiErrorMessage: LiveData<ApiMessage>
        get() = _apiErrorMessage

    fun fetchCurrentQuestion(accessToken: String, questionId: String) {

        var fetchCurrentQuestionListener = Response.Listener<Question> {
            response ->
            _currentQuestion.value = response
        }

        var fetchCurrentQuestionErrorListener = Response.ErrorListener{
            error ->
            if (hasApiErrorMessage(error)) {
                _apiErrorMessage.value = getApiErrorMessage(error)
            } else {
                _apiErrorMessage.value = ApiMessage(getDefaultErrorMessage(error))
            }
        }

        var headers = HashMap<String, String>()
        headers["Authorization"] = accessToken

        var fetchCurrentQuestionGsonRequest = GsonGetRequest<Question>(context?.getString(R.string.base_url) + "questions/" + questionId, Question::class.java, headers, fetchCurrentQuestionListener, fetchCurrentQuestionErrorListener)

        if (context != null) VolleySingleton.getInstance(context).addToRequestQueue(fetchCurrentQuestionGsonRequest)
    }

    fun fetchAnswers(accessToken: String, questionId: String) {

        var fetchAnswersListener = Response.Listener<Answers> {
            response ->
            _answers.value = response
        }

        var fetchAnswersErrorListener = Response.ErrorListener{
            error ->
            if (hasApiErrorMessage(error)) {
                _apiErrorMessage.value = getApiErrorMessage(error)
            } else {
                _apiErrorMessage.value = ApiMessage(getDefaultErrorMessage(error))
            }
        }

        var headers = HashMap<String, String>()
        headers["Authorization"] = accessToken

        var fetchAnswersGsonRequest = GsonGetRequest<Answers>(context?.getString(R.string.base_url) + "answers/" + questionId, Answers::class.java, headers, fetchAnswersListener, fetchAnswersErrorListener)

        if (context != null) VolleySingleton.getInstance(context).addToRequestQueue(fetchAnswersGsonRequest)
    }

    fun likeSwap(accessToken: String, questionId: String, userIsLiking: Boolean) {

        var likeSwapListener = Response.Listener<ApiMessage> {
                response ->
                _likeQuestionMessage.value = response
        }

        var likeSwapErrorListener = Response.ErrorListener{
            error ->
            if (hasApiErrorMessage(error)) {
                _apiErrorMessage.value = getApiErrorMessage(error)
            } else {
                _apiErrorMessage.value = ApiMessage(getDefaultErrorMessage(error))
            }
        }

        var headers = HashMap<String, String>()
        headers["Authorization"] = accessToken

        if (userIsLiking) {
            var userUnlikeGsonRequest = GsonDeleteRequest<ApiMessage>(context.getString(R.string.base_url) + "liked_questions/" + questionId,
                ApiMessage::class.java, headers, likeSwapListener, likeSwapErrorListener)
            VolleySingleton.getInstance(context).addToRequestQueue(userUnlikeGsonRequest)
        } else {
            var userLikeGsonRequest = GsonPostRequest<ApiMessage>(context.getString(R.string.base_url) + "liked_questions/" + questionId,
                null , ApiMessage::class.java, headers, likeSwapListener, likeSwapErrorListener)
            VolleySingleton.getInstance(context).addToRequestQueue(userLikeGsonRequest)
        }

    }

}