package com.example.tddd80application.ui.ownquestions

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tddd80application.activities.AskQuestionActivity
import com.example.tddd80application.activities.QuestionDetailsActivity
import com.example.tddd80application.adapters.OnQuestionItemClickListener
import com.example.tddd80application.adapters.UserFeedQuestionRecyclerAdapter
import com.example.tddd80application.base.BaseFragment
import com.example.tddd80application.databinding.FragmentOwnQuestionsBinding
import com.example.tddd80application.responsedata.Question
import java.io.Serializable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

const val EXTRA_QUESTION_CLICKED_AFAQ = "com.example.tddd80application.CLICKED_QUESTION"

class OwnQuestionsFragment : BaseFragment<OwnQuestionsViewModel, FragmentOwnQuestionsBinding>(), OnQuestionItemClickListener {

    private lateinit var ownQuestionsViewModel: OwnQuestionsViewModel

    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var questionsDataSet: ArrayList<Question>
    private lateinit var mUserFeedQuestionRecyclerAdapter: UserFeedQuestionRecyclerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        ownQuestionsViewModel = ViewModelProvider(this).get(OwnQuestionsViewModel::class.java)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        /* - - - Setup recycler view and adapters - - - */

        viewManager = LinearLayoutManager(requireContext())

        questionsDataSet = arrayListOf()

        mUserFeedQuestionRecyclerAdapter = UserFeedQuestionRecyclerAdapter(questionsDataSet, this)

        viewAdapter = mUserFeedQuestionRecyclerAdapter

        binding.fragmentOwnQuestionsQuestionsRecyclerView.apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        // Update dataset when new set of questions are fetched
        ownQuestionsViewModel.ownQuestions.observe(viewLifecycleOwner, Observer {
            mUserFeedQuestionRecyclerAdapter.updateData(it.questions)
        })

        ownQuestionsViewModel.apiErrorMessage.observe(viewLifecycleOwner, Observer {
            Toast.makeText(requireContext(), it.msg, Toast.LENGTH_LONG).show()
        })

        binding.fragmentOwnQuestionsAskQuestionButton.setOnClickListener {
            requireActivity().startActivity(Intent(this.context, AskQuestionActivity::class.java))
        }

        // Fetch the latest questions for the own questions feed
        lifecycleScope.launch {
            val accessToken = userPreferences.accessToken.first()
            if (accessToken != null && accessToken.isNotEmpty()) {
                ownQuestionsViewModel.fetchOwnQuestions(accessToken)
            }
        }
    }

    override fun getViewModel() = OwnQuestionsViewModel::class.java

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentOwnQuestionsBinding.inflate(inflater, container, false)

    /**
     * Spawn a new activity when clicking on a question in the list, passing along the [Question]
     * to show the details and fetch the answers.
     */
    override fun onQuestionItemClicked(question: Question) {
        val intent = Intent(requireContext(), QuestionDetailsActivity::class.java).apply {
            putExtra(EXTRA_QUESTION_CLICKED_AFAQ, question as Serializable)
        }
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        // Fetch the latest questions for the own questions feed
        lifecycleScope.launch {
            val accessToken = userPreferences.accessToken.first()
            if (accessToken != null && accessToken.isNotEmpty()) {
                ownQuestionsViewModel.fetchOwnQuestions(accessToken)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mUserFeedQuestionRecyclerAdapter.setLifecycleDestroyed()
    }

}