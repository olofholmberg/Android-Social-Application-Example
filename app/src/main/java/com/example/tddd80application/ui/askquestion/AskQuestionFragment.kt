package com.example.tddd80application.ui.askquestion

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.tddd80application.R
import com.example.tddd80application.base.BaseFragment
import com.example.tddd80application.databinding.FragmentAskQuestionBinding
import com.example.tddd80application.extensions.enable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AskQuestionFragment : BaseFragment<AskQuestionViewModel, FragmentAskQuestionBinding>() {

    private lateinit var askQuestionViewModel: AskQuestionViewModel
    private lateinit var availableCourses: Array<String>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        askQuestionViewModel = ViewModelProvider(this).get(AskQuestionViewModel::class.java)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        availableCourses = arrayOf("Select a course...")

        // Update dataset when new set of courses are fetched
        askQuestionViewModel.availableCourses.observe(viewLifecycleOwner, Observer {

            val fetchedCourses: ArrayList<String> = arrayListOf("Select a course...")
            it.courses.forEach {vmCoursesIt->
                fetchedCourses.add(vmCoursesIt.course_code)
            }

            availableCourses = fetchedCourses.toTypedArray()

            // Create new adapter with the fetched courses
            val newAvailableCoursesArrayAdapter = object: ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, availableCourses) {

                override fun isEnabled(position: Int): Boolean {
                    return (position != 0)
                }

                override fun getDropDownView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup
                ): View {
                    val view = super.getDropDownView(position, convertView, parent)
                    val tv = view as TextView
                    if (position == 0)
                        tv.setTextColor(Color.GRAY)
                    else
                        tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorOnSurface))
                    return super.getDropDownView(position, convertView, parent)
                }
            }
            newAvailableCoursesArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.fragmentAskQuestionCourseSelectionSpinner.adapter = newAvailableCoursesArrayAdapter
        })

        binding.fragmentAskQuestionPostButton.enable(false)

        // Enable answer button when text in question title
        binding.fragmentAskQuestionTitleInputEdit.addTextChangedListener {
            val body = binding.fragmentAskQuestionTitleInputEdit.text.toString()
            binding.fragmentAskQuestionPostButton.enable(body.isNotEmpty() && it.toString().isNotEmpty())
        }

        // Add listener for updated api error message on failed question posting
        askQuestionViewModel.apiErrorMessage.observe(viewLifecycleOwner, Observer {
            when(it.msg) {
                "This course does not exist" -> courseError(it.msg)
                else -> Toast.makeText(requireContext(), it.msg, Toast.LENGTH_LONG).show()
            }
        })

        // Fetch the available courses for the spinner
        lifecycleScope.launch {
            val accessToken = userPreferences.accessToken.first()
            if (accessToken != null && accessToken.isNotEmpty()) {
                askQuestionViewModel.fetchAvailableCourses(accessToken)
            }
        }

        // Add ask question button click listener
        binding.fragmentAskQuestionPostButton.setOnClickListener {

            lifecycleScope.launch {
                val accessToken = userPreferences.accessToken.first()
                if (accessToken != null && accessToken.isNotEmpty()) {
                    val questionTitle = binding.fragmentAskQuestionTitleInputEdit.text.toString()
                    val courseCode = binding.fragmentAskQuestionCourseSelectionSpinner.selectedItem.toString()
                    val questionDescription = binding.fragmentAskQuestionBodyInputEdit.text.toString()

                    askQuestionViewModel.postQuestion(accessToken, questionTitle, courseCode, questionDescription)
                }
            }
        }

        // Add listener for successful question posting
        askQuestionViewModel.askQuestionMessage.observe(viewLifecycleOwner, Observer {
            when(it.msg) {
                "Question successfully added" -> {
                    lifecycleScope.launch {
                        Toast.makeText(requireContext(), it.msg, Toast.LENGTH_LONG).show()
                        requireActivity().finish() // Navigate back through finish of parent activity
                    }
                }
            }
        })
    }

    override fun getViewModel() = AskQuestionViewModel::class.java

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentAskQuestionBinding.inflate(inflater, container, false)

    private fun courseError(errorMsg: String) {
        binding.fragmentAskQuestionCourseSelectionSpinner.setBackgroundResource(R.drawable.input_fragment_background_error)
        binding.fragmentAskQuestionCourseErrorTextView.text = errorMsg
        binding.fragmentAskQuestionCourseErrorTextView.visibility = View.VISIBLE
    }

}