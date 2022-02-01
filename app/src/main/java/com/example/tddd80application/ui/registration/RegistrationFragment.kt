package com.example.tddd80application.ui.registration

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.example.tddd80application.R
import com.example.tddd80application.base.BaseFragment
import com.example.tddd80application.databinding.FragmentRegistrationBinding
import com.example.tddd80application.extensions.enable
import kotlinx.coroutines.launch

class RegistrationFragment : BaseFragment<RegistrationViewModel, FragmentRegistrationBinding>() {

    private lateinit var registrationViewModel: RegistrationViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        registrationViewModel = ViewModelProvider(this).get(RegistrationViewModel::class.java)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.fragmentRegistrationRegisterAccountButton.enable(false)

        // Add listener for updated message on successful registration
        registrationViewModel.registrationMessage.observe(viewLifecycleOwner, Observer {
            when(it.msg) {
                "User successfully registered" -> {
                    lifecycleScope.launch {
                        Toast.makeText(context, it.msg, Toast.LENGTH_LONG).show()
                        view?.findNavController()?.navigate(R.id.action_navigation_registration_to_navigation_login)
                    }
                }
            }
        })

        // Add listener for updated api error message on failed registration
        registrationViewModel.apiErrorMessage.observe(viewLifecycleOwner, Observer {
            when(it.msg) {
                "Username is already taken" -> usernameError(it.msg)
                "Email is already registered" -> emailError(it.msg)
                else -> Toast.makeText(requireContext(), it.msg, Toast.LENGTH_LONG).show()
            }
        })

        // - - - Button Enabling Listeners on Input Fields

        binding.fragmentRegistrationUsernameInputEdit.addTextChangedListener {
            val username = binding.fragmentRegistrationUsernameInputEdit.text.toString()
            val email = binding.fragmentRegistrationEmailInputEdit.text.toString()
            val password = binding.fragmentRegistrationPasswordInputEdit.text.toString()
            val confPassword = binding.fragmentRegistrationPasswordConfirmationInputEdit.text.toString()
            binding.fragmentRegistrationRegisterAccountButton.enable(username.isNotEmpty() and email.isNotEmpty() and password.isNotEmpty() and confPassword.isNotEmpty() and it.toString().isNotEmpty())
        }

        binding.fragmentRegistrationEmailInputEdit.addTextChangedListener {
            val username = binding.fragmentRegistrationUsernameInputEdit.text.toString()
            val email = binding.fragmentRegistrationEmailInputEdit.text.toString()
            val password = binding.fragmentRegistrationPasswordInputEdit.text.toString()
            val confPassword = binding.fragmentRegistrationPasswordConfirmationInputEdit.text.toString()
            binding.fragmentRegistrationRegisterAccountButton.enable(username.isNotEmpty() and email.isNotEmpty() and password.isNotEmpty() and confPassword.isNotEmpty() and it.toString().isNotEmpty())
        }

        binding.fragmentRegistrationPasswordInputEdit.addTextChangedListener {
            val username = binding.fragmentRegistrationUsernameInputEdit.text.toString()
            val email = binding.fragmentRegistrationEmailInputEdit.text.toString()
            val password = binding.fragmentRegistrationPasswordInputEdit.text.toString()
            val confPassword = binding.fragmentRegistrationPasswordConfirmationInputEdit.text.toString()
            binding.fragmentRegistrationRegisterAccountButton.enable(username.isNotEmpty() and email.isNotEmpty() and password.isNotEmpty() and confPassword.isNotEmpty() and it.toString().isNotEmpty())
        }

        binding.fragmentRegistrationPasswordConfirmationInputEdit.addTextChangedListener {
            val username = binding.fragmentRegistrationUsernameInputEdit.text.toString()
            val email = binding.fragmentRegistrationEmailInputEdit.text.toString()
            val password = binding.fragmentRegistrationPasswordInputEdit.text.toString()
            val confPassword = binding.fragmentRegistrationPasswordConfirmationInputEdit.text.toString()
            binding.fragmentRegistrationRegisterAccountButton.enable(username.isNotEmpty() and email.isNotEmpty() and password.isNotEmpty() and confPassword.isNotEmpty() and it.toString().isNotEmpty())
        }

        // - - - Button on click listeners - - -

        binding.fragmentRegistrationRegisterAccountButton.setOnClickListener {

            resetErrors()

            val username = binding.fragmentRegistrationUsernameInputEdit.text.toString()
            val email = binding.fragmentRegistrationEmailInputEdit.text.toString()
            val password = binding.fragmentRegistrationPasswordInputEdit.text.toString()
            val confPassword = binding.fragmentRegistrationPasswordConfirmationInputEdit.text.toString()

            if (isValidForm(username, email, password, confPassword)) {
                registrationViewModel.register(username, email, password)
            }
        }

        binding.fragmentRegistrationBackToLoginButton.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_navigation_registration_to_navigation_login)
        }
    }

    override fun getViewModel() = RegistrationViewModel::class.java

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentRegistrationBinding.inflate(inflater, container, false)

    private fun usernameError(msg:String) {
        binding.fragmentRegistrationUsernameInputEdit.setBackgroundResource(R.drawable.input_fragment_background_error)
        binding.fragmentRegistrationUsernameErrorTextView.text = msg
        binding.fragmentRegistrationUsernameErrorTextView.visibility = View.VISIBLE
    }

    private fun emailError(msg:String) {
        binding.fragmentRegistrationEmailInputEdit.setBackgroundResource(R.drawable.input_fragment_background_error)
        binding.fragmentRegistrationEmailErrorTextView.text = msg
        binding.fragmentRegistrationEmailErrorTextView.visibility = View.VISIBLE
    }

    private fun resetErrors() {
        binding.fragmentRegistrationUsernameInputEdit.setBackgroundResource(R.drawable.input_fragment_background_selector)
        binding.fragmentRegistrationUsernameErrorTextView.visibility = View.INVISIBLE
        binding.fragmentRegistrationEmailInputEdit.setBackgroundResource(R.drawable.input_fragment_background_selector)
        binding.fragmentRegistrationEmailErrorTextView.visibility = View.INVISIBLE
        binding.fragmentRegistrationPasswordInputEdit.setBackgroundResource(R.drawable.input_fragment_background_selector)
        binding.fragmentRegistrationPasswordErrorTextView.visibility = View.INVISIBLE
        binding.fragmentRegistrationPasswordConfirmationInputEdit.setBackgroundResource(R.drawable.input_fragment_background_selector)
        binding.fragmentRegistrationPasswordConfirmationErrorTextView.visibility = View.INVISIBLE
    }

    private fun isValidForm(username: String, email: String, password: String, confPassword: String): Boolean {
        if (isValidUsername(username) and isValidEmail(email) and isValidPassword(password) and isMatchingPassword(password, confPassword)) {
            return true
        }
        return false
    }

    private fun isValidUsername(username: String): Boolean {
        if (username.isEmpty()) {
            binding.fragmentRegistrationUsernameInputEdit.setBackgroundResource(R.drawable.input_fragment_background_error)
            binding.fragmentRegistrationUsernameErrorTextView.text = "You have to enter a username"
            binding.fragmentRegistrationUsernameErrorTextView.visibility = View.VISIBLE
            return false
        }
        return true
    }

    private fun isValidEmail(email: String): Boolean {
        if (email.isEmpty()) {
            binding.fragmentRegistrationEmailInputEdit.setBackgroundResource(R.drawable.input_fragment_background_error)
            binding.fragmentRegistrationEmailErrorTextView.text = "You have to enter an email address"
            binding.fragmentRegistrationEmailErrorTextView.visibility = View.VISIBLE
            return false
        }
        return true
    }

    private fun isValidPassword(password: String): Boolean {
        if (password.isEmpty()) {
            binding.fragmentRegistrationPasswordInputEdit.setBackgroundResource(R.drawable.input_fragment_background_error)
            binding.fragmentRegistrationPasswordErrorTextView.text = "You have to enter a password"
            binding.fragmentRegistrationPasswordErrorTextView.visibility = View.VISIBLE
            return false
        }
        return true
    }

    private fun isMatchingPassword(password: String, confirmPassword: String): Boolean {
        if (!TextUtils.equals(password, confirmPassword)) {
            binding.fragmentRegistrationPasswordConfirmationInputEdit.setBackgroundResource(R.drawable.input_fragment_background_error)
            binding.fragmentRegistrationPasswordConfirmationErrorTextView.text = "Passwords do not match"
            binding.fragmentRegistrationPasswordConfirmationErrorTextView.visibility = View.VISIBLE
            return false
        }
        return true
    }


}