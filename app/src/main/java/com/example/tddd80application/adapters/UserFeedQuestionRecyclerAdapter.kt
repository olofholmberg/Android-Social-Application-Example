package com.example.tddd80application.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.example.tddd80application.R
import com.example.tddd80application.data.UserPreferences
import com.example.tddd80application.extensions.strToAppTimestamp
import com.example.tddd80application.network.*
import com.example.tddd80application.responsedata.ApiMessage
import com.example.tddd80application.responsedata.Question
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class UserFeedQuestionRecyclerAdapter(private var myDataSet: ArrayList<Question>, private val questionItemClickListener: OnQuestionItemClickListener) : RecyclerView.Adapter<UserFeedQuestionRecyclerAdapter.UserFeedQuestionViewHolder>() {

    private val userFeedQuestionViewHolders: MutableList<UserFeedQuestionViewHolder> = mutableListOf()

    class UserFeedQuestionViewHolder(view: View) : RecyclerView.ViewHolder(view), LifecycleOwner {

        private val lifecycleRegistry = LifecycleRegistry(this)
        private var wasPaused: Boolean = false

        private lateinit var questionData: Question
        private val questionTitleTextView: TextView = view.findViewById(R.id.recycler_question_title_text_view)
        private val questionAuthorTextView: TextView = view.findViewById(R.id.recycler_question_author_text_view)
        private val questionTimestampTextView: TextView = view.findViewById(R.id.recycler_question_timestamp_text_view)
        private val questionCourseTextView: TextView = view.findViewById(R.id.recycler_question_course_text_view)
        private val questionBodyTextView: TextView = view.findViewById(R.id.recycler_question_body_text_view)
        private val questionLikeButton: MaterialButton = view.findViewById(R.id.recycler_question_like_button)
        private val questionLikesTextView: TextView = view.findViewById(R.id.recycler_question_likes_text_view)
        private val questionAnswersTextView: TextView = view.findViewById(R.id.recycler_question_answers_text_view)
        private var userIsLiking: Boolean = false

        /* - - - Lifecycle methods - - - */

        init {
            lifecycleRegistry.currentState = Lifecycle.State.INITIALIZED
        }

        fun markCreated() {
            lifecycleRegistry.currentState = Lifecycle.State.CREATED
        }

        fun markAttach() {
            if (wasPaused) {
                lifecycleRegistry.currentState = Lifecycle.State.RESUMED
                wasPaused = false
            } else {
                lifecycleRegistry.currentState = Lifecycle.State.STARTED
            }
        }

        fun markDetach() {
            wasPaused = true
            lifecycleRegistry.currentState = Lifecycle.State.CREATED
        }

        fun markDestroyed() {
            lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        }

        override fun getLifecycle(): Lifecycle {
            return lifecycleRegistry
        }

        fun bind(question: Question, clickListener: OnQuestionItemClickListener) {

            questionData = question
            questionTitleTextView.text = question.question_title
            questionAuthorTextView.text = question.author.username
            questionTimestampTextView.text = "at " + question.timestamp.strToAppTimestamp()
            questionCourseTextView.text = question.course.course_code
            questionBodyTextView.text = question.question_body
            questionLikeButton.apply {
                setOnClickListener {
                    lifecycleScope.launch {
                        var userPreferences = UserPreferences(context)
                        val accessToken = userPreferences.accessToken.first()
                        if (accessToken != null && accessToken.isNotEmpty()) {
                            likeSwap(accessToken, context)
                        }
                    }
                }
            }
            questionLikesTextView.text = question.likes
            questionAnswersTextView.text = question.answers
            userIsLiking = question.is_liking == "True"
            updateLikeButtonTextAndIcon()

            itemView.setOnClickListener {
                clickListener.onQuestionItemClicked(question)
            }
        }

        private fun updateLikeButtonTextAndIcon() {
            if (userIsLiking) {
                questionLikeButton.text = "Unlike"
                questionLikeButton.setIconResource(R.drawable.ic_unlike_question_24dp)
            } else {
                questionLikeButton.text = "Like"
                questionLikeButton.setIconResource(R.drawable.ic_like_question_24dp)
            }
        }

        /**
         * Sends the appropriate request to swap the like status for the question that the
         * like/unlike button was clicked on.
         */
        private fun likeSwap(accessToken: String, context: Context) {

            var likeSwapListener = Response.Listener<ApiMessage> { response ->
                when(response.msg) {
                    "Like successful" -> {
                        userIsLiking = true
                        questionLikesTextView.text = (questionLikesTextView.text.toString().toInt() + 1).toString()
                    }
                    "Unlike successful" -> {
                        userIsLiking = false
                        questionLikesTextView.text = (questionLikesTextView.text.toString().toInt() - 1).toString()
                    }
                }
                updateLikeButtonTextAndIcon()
            }

            var likeSwapErrorListener = Response.ErrorListener{ error ->
                if (hasApiErrorMessage(error)) {
                    Toast.makeText(context, getApiErrorMessage(error).msg , Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, getDefaultErrorMessage(error) , Toast.LENGTH_LONG).show()
                }
            }

            var headers = HashMap<String, String>()
            headers["Authorization"] = accessToken

            if (userIsLiking) {
                var userUnlikeGsonRequest = GsonDeleteRequest<ApiMessage>(context.getString(R.string.base_url) + "liked_questions/" + questionData.question_id,
                        ApiMessage::class.java, headers, likeSwapListener, likeSwapErrorListener)
                VolleySingleton.getInstance(context).addToRequestQueue(userUnlikeGsonRequest)
            } else {
                var userLikeGsonRequest = GsonPostRequest<ApiMessage>(context.getString(R.string.base_url) + "liked_questions/" + questionData.question_id,
                        null , ApiMessage::class.java, headers, likeSwapListener, likeSwapErrorListener)
                VolleySingleton.getInstance(context).addToRequestQueue(userLikeGsonRequest)
            }

        }

    }

    /** Invoked by the layout manager (creates new views) */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserFeedQuestionViewHolder {
        val questionViewHolder = UserFeedQuestionViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recycler_question_view, parent, false))
        questionViewHolder.markCreated()
        userFeedQuestionViewHolders.add(questionViewHolder)
        return questionViewHolder
    }

    /** Invoked by the layout manager (replaces the contents of a view). */
    override fun onBindViewHolder(holder: UserFeedQuestionViewHolder, position: Int) {
        /**
         * Fetch the correct question from the data set based on the position and replace the
         * view contents based on the fetched question.
         */
        val question = myDataSet[position]
        holder.bind(question, questionItemClickListener)
    }

    fun updateData(data: ArrayList<Question>) {
        myDataSet = data
        notifyDataSetChanged()
    }

    /** Invoked by the layout manager (should return size of the data set). */
    override fun getItemCount() = myDataSet.size

    /* - - - ViewHolder Lifecycle Management - - - */

    override fun onViewAttachedToWindow(holder: UserFeedQuestionViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.markAttach()
    }

    override fun onViewDetachedFromWindow(holder: UserFeedQuestionViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.markDetach()
    }

    fun setLifecycleDestroyed() {
        userFeedQuestionViewHolders.forEach {
            it.markDestroyed()
        }
    }
}

interface OnQuestionItemClickListener {
    fun onQuestionItemClicked(question: Question)
}