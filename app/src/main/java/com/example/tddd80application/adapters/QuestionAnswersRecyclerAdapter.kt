package com.example.tddd80application.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.recyclerview.widget.RecyclerView
import com.example.tddd80application.R
import com.example.tddd80application.extensions.strToAppTimestamp
import com.example.tddd80application.responsedata.Answer

class QuestionAnswersRecyclerAdapter(private var myDataSet: ArrayList<Answer>) : RecyclerView.Adapter<QuestionAnswersRecyclerAdapter.QuestionAnswerViewHolder>() {

    private val questionAnswersViewHolders: MutableList<QuestionAnswerViewHolder> = mutableListOf()

    class QuestionAnswerViewHolder(view: View) : RecyclerView.ViewHolder(view), LifecycleOwner {

        private val lifecycleRegistry = LifecycleRegistry(this)
        private var wasPaused: Boolean = false

        private lateinit var answerData: Answer
        private val answerAuthorTextView: TextView = view.findViewById(R.id.recycler_answer_author_text_view)
        private val answerTimestampTextView: TextView = view.findViewById(R.id.recycler_answer_timestamp_text_view)
        private val answerBodyTextView: TextView = view.findViewById(R.id.recycler_answer_body_text_view)

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

        fun bind(answer: Answer) {

            answerData = answer
            answerAuthorTextView.text = answer.author.username
            answerTimestampTextView.text = "at " + answer.timestamp.strToAppTimestamp()
            answerBodyTextView.text = answer.answer_body

        }

    }

    /** Invoked by the layout manager (creates new views) */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionAnswerViewHolder {
        val questionAnswerViewHolder = QuestionAnswerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recycler_answer_view, parent, false))
        questionAnswerViewHolder.markCreated()
        questionAnswersViewHolders.add(questionAnswerViewHolder)
        return questionAnswerViewHolder
    }

    /** Invoked by the layout manager (replaces the contents of a view). */
    override fun onBindViewHolder(holder: QuestionAnswerViewHolder, position: Int) {
        /**
         * Fetch the correct question from the data set based on the position and replace the
         * view contents based on the fetched question.
         */
        val answer = myDataSet[position]
        holder.bind(answer)
    }

    fun updateData(data: ArrayList<Answer>) {
        myDataSet = data
        notifyDataSetChanged()
    }

    /** Invoked by the layout manager (should return size of the data set). */
    override fun getItemCount() = myDataSet.size

    /* - - - ViewHolder Lifecycle Management - - - */

    override fun onViewAttachedToWindow(holder: QuestionAnswerViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.markAttach()
    }

    override fun onViewDetachedFromWindow(holder: QuestionAnswerViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.markDetach()
    }

    fun setLifecycleDestroyed() {
        questionAnswersViewHolders.forEach {
            it.markDestroyed()
        }
    }
}