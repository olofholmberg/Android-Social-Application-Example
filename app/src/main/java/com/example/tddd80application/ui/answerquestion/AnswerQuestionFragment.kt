package com.example.tddd80application.ui.answerquestion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.example.tddd80application.R
import com.example.tddd80application.base.BaseFragment
import com.example.tddd80application.databinding.FragmentAnswerQuestionBinding
import com.example.tddd80application.extensions.enable
import com.example.tddd80application.responsedata.Question
import com.example.tddd80application.ui.ownquestions.EXTRA_QUESTION_CLICKED_AFAQ
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AnswerQuestionFragment : BaseFragment<AnswerQuestionViewModel, FragmentAnswerQuestionBinding>() {

    private lateinit var answerQuestionViewModel: AnswerQuestionViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        answerQuestionViewModel = ViewModelProvider(this).get(AnswerQuestionViewModel::class.java)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val question = requireActivity().intent.getSerializableExtra(EXTRA_QUESTION_CLICKED_AFAQ) as Question

        binding.fragmentAnswerQuestionPostButton.enable(false)

        // Enable answer button when text in answer
        binding.fragmentAnswerQuestionBodyInputEdit.addTextChangedListener {
            val body = binding.fragmentAnswerQuestionBodyInputEdit.text.toString()
            binding.fragmentAnswerQuestionPostButton.enable(body.isNotEmpty() && it.toString().isNotEmpty())
        }

        // Add answer question button click listener
        binding.fragmentAnswerQuestionPostButton.setOnClickListener {
            resetErrors()
            val body = binding.fragmentAnswerQuestionBodyInputEdit.text.toString()
            if (isValidBody(body)) {
                lifecycleScope.launch {
                    val accessToken = userPreferences.accessToken.first()
                    if (accessToken != null && accessToken.isNotEmpty()) {
                        answerQuestionViewModel.postAnswer(accessToken, body, question.question_id)
                    }
                }
            }
        }

        answerQuestionViewModel.answerQuestionMessage.observe(viewLifecycleOwner, Observer {
            when(it.msg) {
                "Question successfully answered" -> navigateBack()
            }
        })

        answerQuestionViewModel.apiErrorMessage.observe(viewLifecycleOwner, Observer {
            Toast.makeText(requireContext(), it.msg, Toast.LENGTH_LONG).show()
        })

        // Add back navigation button click listener
        binding.fragmentAnswerQuestionGoToQuestionDetailsButton.setOnClickListener {
            navigateBack()
        }
    }

    override fun getViewModel() = AnswerQuestionViewModel::class.java

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentAnswerQuestionBinding.inflate(inflater, container, false)

    private fun resetErrors() {
        binding.fragmentAnswerQuestionBodyInputEdit.setBackgroundResource(R.drawable.input_fragment_background_selector)
        binding.fragmentAnswerQuestionBodyErrorTextView.visibility = View.INVISIBLE
    }

    private fun emptyAnswerInputError() {
        binding.fragmentAnswerQuestionBodyInputEdit.setBackgroundResource(R.drawable.input_fragment_background_error)
        binding.fragmentAnswerQuestionBodyErrorTextView.text = getString(R.string.fragment_answer_question_body_error)
        binding.fragmentAnswerQuestionBodyErrorTextView.visibility = View.VISIBLE
    }

    private fun isValidBody(body: String): Boolean {
        if (body.isEmpty()) {
            emptyAnswerInputError()
            return false
        }
        return true
    }

    private fun navigateBack() {
        view?.findNavController()?.navigate(R.id.action_navigation_answer_question_to_navigation_question_details)
    }

}