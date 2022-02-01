package com.example.tddd80application.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.tddd80application.activities.AuthActivity
import com.example.tddd80application.base.BaseFragment
import com.example.tddd80application.databinding.FragmentProfileBinding
import com.example.tddd80application.extensions.startNewActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProfileFragment : BaseFragment<ProfileViewModel, FragmentProfileBinding>() {

    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Update profile when user is fetched
        profileViewModel.currentUser.observe(viewLifecycleOwner, Observer {
            binding.fragmentProfileUsernameText.text = it.username
            binding.fragmentProfileUserIdText.text = it.user_id
            binding.fragmentProfileEmailText.text = it.email
        })

        // Fetch the latest questions for the user feed
        lifecycleScope.launch {
            val accessToken = userPreferences.accessToken.first()
            if (accessToken != null && accessToken.isNotEmpty()) {
                profileViewModel.fetchCurrentUser(accessToken)
                binding.fragmentProfileTokenText.text = accessToken
            }
        }

        binding.fragmentProfileLogoutButton.setOnClickListener {
            // Fetch the latest questions for the user feed
            lifecycleScope.launch {
                val accessToken = userPreferences.accessToken.first()
                if (accessToken != null && accessToken.isNotEmpty()) {
                    profileViewModel.logout(accessToken)
                }
            }
        }

        // Add listener for successful logout
        profileViewModel.logoutMessage.observe(viewLifecycleOwner, Observer {
            when(it.msg) {
                "Logout successful" -> {
                    lifecycleScope.launch {
                        userPreferences.saveAccessToken("")
                        requireActivity().startNewActivity(AuthActivity::class.java)
                    }
                }
            }
        })

        profileViewModel.apiErrorMessage.observe(viewLifecycleOwner, Observer {
            Toast.makeText(requireContext(), it.msg, Toast.LENGTH_LONG).show()
        })
    }

    override fun getViewModel() = ProfileViewModel::class.java

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentProfileBinding.inflate(inflater, container, false)
}