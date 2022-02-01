package com.example.tddd80application.ui.login

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
import com.example.tddd80application.activities.MainActivity
import com.example.tddd80application.base.BaseFragment
import com.example.tddd80application.databinding.FragmentLoginBinding
import com.example.tddd80application.extensions.enable
import com.example.tddd80application.extensions.startNewActivity
import kotlinx.coroutines.launch

class LoginFragment : BaseFragment<LoginViewModel, FragmentLoginBinding>() {

    private lateinit var loginViewModel: LoginViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.fragmentLoginLoginButton.enable(false)

        // Add listener for updated token on successful login
        loginViewModel.accessToken.observe(viewLifecycleOwner, Observer {
            when(it.token_type) {
                "Bearer" -> {
                    lifecycleScope.launch {
                        userPreferences.saveAccessToken(it.token_type + " " + it.access_token)
                        requireActivity().startNewActivity(MainActivity::class.java)
                    }
                }
            }
        })

        // Add listener for updated api error message on failed login
        loginViewModel.apiErrorMessage.observe(viewLifecycleOwner, Observer {
            when(it.msg) {
                "Wrong Email" -> emailError(it.msg)
                "Wrong Password" -> passwordError(it.msg)
                else -> Toast.makeText(requireContext(), it.msg, Toast.LENGTH_LONG).show()
            }
        })

        // Enable login button on email input
        binding.fragmentLoginEmailInputEdit.addTextChangedListener {
            val email = binding.fragmentLoginEmailInputEdit.text.toString()
            binding.fragmentLoginLoginButton.enable(email.isNotEmpty() && it.toString().isNotEmpty())
        }

        // Add login button on click listener
        binding.fragmentLoginLoginButton.setOnClickListener {

            val email = binding.fragmentLoginEmailInputEdit.text.toString()
            val password = binding.fragmentLoginPasswordInputEdit.text.toString()

            loginViewModel.login(email, password)
        }

        // Add navigate to registration button on click listener
        binding.fragmentLoginGoToRegistrationButton.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_navigation_login_to_navigation_registration)
        }
    }

    override fun getViewModel() = LoginViewModel::class.java

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentLoginBinding.inflate(inflater, container, false)

    private fun emailError(msg:String) {
        binding.fragmentLoginEmailInputEdit.setBackgroundResource(R.drawable.input_fragment_background_error)
        binding.fragmentLoginEmailErrorTextView.text = msg
        binding.fragmentLoginEmailErrorTextView.visibility = View.VISIBLE
    }

    private fun passwordError(msg:String) {
        binding.fragmentLoginPasswordInputEdit.setBackgroundResource(R.drawable.input_fragment_background_error)
        binding.fragmentLoginPasswordErrorTextView.text = msg
        binding.fragmentLoginPasswordErrorTextView.visibility = View.VISIBLE
    }

}