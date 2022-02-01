package com.example.tddd80application.ui.questiondetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tddd80application.R
import com.example.tddd80application.adapters.QuestionAnswersRecyclerAdapter
import com.example.tddd80application.base.BaseFragment
import com.example.tddd80application.data.UserPreferences
import com.example.tddd80application.databinding.FragmentQuestionDetailsBinding
import com.example.tddd80application.extensions.strToAppTimestamp
import com.example.tddd80application.responsedata.Answer
import com.example.tddd80application.responsedata.Question
import com.example.tddd80application.ui.ownquestions.EXTRA_QUESTION_CLICKED_AFAQ
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class QuestionDetailsFragment : BaseFragment<QuestionDetailsViewModel, FragmentQuestionDetailsBinding>() {

    private lateinit var questionDetailsViewModel: QuestionDetailsViewModel

    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var answersDataSet: ArrayList<Answer>
    private lateinit var mQuestionAnswersRecyclerAdapter: QuestionAnswersRecyclerAdapter

    private lateinit var currentQuestion: Question
    private var userIsLiking: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        questionDetailsViewModel = ViewModelProvider(this).get(QuestionDetailsViewModel::class.java)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        currentQuestion = requireActivity().intent.getSerializableExtra(EXTRA_QUESTION_CLICKED_AFAQ) as Question

        fillQuestionDataFields(currentQuestion)

        binding.fragmentQuestionDetailsLikeButton.apply {
            setOnClickListener {
                lifecycleScope.launch {
                    var userPreferences = UserPreferences(context)
                    val accessToken = userPreferences.accessToken.first()
                    if (accessToken != null && accessToken.isNotEmpty()) {
                        questionDetailsViewModel.likeSwap(accessToken, currentQuestion.question_id, userIsLiking)
                    }
                }
            }
        }

        /* - - - Setup recycler view and adapters - - - */

        viewManager = LinearLayoutManager(requireContext())

        answersDataSet = arrayListOf()

        mQuestionAnswersRecyclerAdapter = QuestionAnswersRecyclerAdapter(answersDataSet)

        viewAdapter = mQuestionAnswersRecyclerAdapter

        binding.fragmentQuestionDetailsRecyclerView.apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        // Update dataset and number of answers when new set of questions are fetched
        questionDetailsViewModel.answers.observe(viewLifecycleOwner, Observer {
            mQuestionAnswersRecyclerAdapter.updateData(it.answers)
            binding.fragmentQuestionDetailsAnswersTextView.text = it.answers.size.toString()
        })

        questionDetailsViewModel.currentQuestion.observe(viewLifecycleOwner, Observer {
            fillQuestionDataFields(it)
        })

        questionDetailsViewModel.likeQuestionMessage.observe(viewLifecycleOwner, Observer {
            when(it.msg) {
                "Like successful" -> {
                    userIsLiking = true
                    binding.fragmentQuestionDetailsLikesTextView.text = (binding.fragmentQuestionDetailsLikesTextView.text.toString().toInt() + 1).toString()
                }
                "Unlike successful" -> {
                    userIsLiking = false
                    binding.fragmentQuestionDetailsLikesTextView.text = (binding.fragmentQuestionDetailsLikesTextView.text.toString().toInt() - 1).toString()
                }
            }
            updateLikeButtonTextAndIcon()
        })

        questionDetailsViewModel.apiErrorMessage.observe(viewLifecycleOwner, Observer {
            Toast.makeText(requireContext(), it.msg, Toast.LENGTH_LONG).show()
        })

        binding.fragmentQuestionDetailsAnswerButton.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_navigation_question_details_to_navigation_answer_question)
        }

        // Fetch the latest answers and question data for the question's answer feed
        lifecycleScope.launch {
            val accessToken = userPreferences.accessToken.first()
            if (accessToken != null && accessToken.isNotEmpty()) {
                questionDetailsViewModel.fetchAnswers(accessToken, currentQuestion.question_id)
                questionDetailsViewModel.fetchCurrentQuestion(accessToken, currentQuestion.question_id)
            }
        }
    }

    private fun fillQuestionDataFields(question: Question) {
        binding.fragmentQuestionDetailsTitleTextView.text = question.question_title
        binding.fragmentQuestionDetailsAuthorTextView.text = question.author.username
        binding.fragmentQuestionDetailsTimestampTextView.text = "at " + question.timestamp.strToAppTimestamp()
        binding.fragmentQuestionDetailsCourseTextView.text = question.course.course_code
        binding.fragmentQuestionDetailsBodyTextView.text = question.question_body
        userIsLiking = question.is_liking == "True"
        binding.fragmentQuestionDetailsLikesTextView.text = question.likes
        binding.fragmentQuestionDetailsAnswersTextView.text = question.answers
        updateLikeButtonTextAndIcon()
    }

    private fun updateLikeButtonTextAndIcon() {
        if (userIsLiking) {
            binding.fragmentQuestionDetailsLikeButton.text = "Unlike"
            binding.fragmentQuestionDetailsLikeButton.setIconResource(R.drawable.ic_unlike_question_24dp)
        } else {
            binding.fragmentQuestionDetailsLikeButton.text = "Like"
            binding.fragmentQuestionDetailsLikeButton.setIconResource(R.drawable.ic_like_question_24dp)
        }
    }

    override fun getViewModel() = QuestionDetailsViewModel::class.java

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentQuestionDetailsBinding.inflate(inflater, container, false)

    override fun onResume() {
        super.onResume()
        // Fetch the latest answers and question data for the question's answer feed
        lifecycleScope.launch {
            val accessToken = userPreferences.accessToken.first()
            if (accessToken != null && accessToken.isNotEmpty()) {
                currentQuestion = requireActivity().intent.getSerializableExtra(EXTRA_QUESTION_CLICKED_AFAQ) as Question
                questionDetailsViewModel.fetchAnswers(accessToken, currentQuestion.question_id)
                questionDetailsViewModel.fetchCurrentQuestion(accessToken, currentQuestion.question_id)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mQuestionAnswersRecyclerAdapter.setLifecycleDestroyed()
    }

}