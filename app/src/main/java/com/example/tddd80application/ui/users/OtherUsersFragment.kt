package com.example.tddd80application.ui.users

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
import com.example.tddd80application.adapters.OnOtherUserItemClickListener
import com.example.tddd80application.adapters.OtherUserRecyclerAdapter
import com.example.tddd80application.base.BaseFragment
import com.example.tddd80application.databinding.FragmentOtherUsersBinding
import com.example.tddd80application.responsedata.OtherUser
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class OtherUsersFragment : BaseFragment<OtherUsersViewModel, FragmentOtherUsersBinding>(), OnOtherUserItemClickListener {

    private lateinit var otherUsersViewModel: OtherUsersViewModel

    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var otherUsersDataSet: ArrayList<OtherUser>
    private lateinit var mOtherUsersRecyclerAdapter: OtherUserRecyclerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        otherUsersViewModel = ViewModelProvider(this).get(OtherUsersViewModel::class.java)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        /* - - - Setup recycler view and adapters - - - */

        viewManager = LinearLayoutManager(requireContext())

        otherUsersDataSet = arrayListOf()

        mOtherUsersRecyclerAdapter = OtherUserRecyclerAdapter(otherUsersDataSet, this)

        viewAdapter = mOtherUsersRecyclerAdapter

        binding.fragmentOtherUsersRecyclerView.apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        // Update dataset when new set of users are fetched
        otherUsersViewModel.otherUsers.observe(viewLifecycleOwner, Observer {
            mOtherUsersRecyclerAdapter.updateData(it.users)
        })

        otherUsersViewModel.apiErrorMessage.observe(viewLifecycleOwner, Observer {
            Toast.makeText(requireContext(), it.msg, Toast.LENGTH_LONG).show()
        })

        // Fetch the users for the list
        lifecycleScope.launch {
            val accessToken = userPreferences.accessToken.first()
            if (accessToken != null && accessToken.isNotEmpty()) {
                otherUsersViewModel.fetchOtherUsers(accessToken)
            }
        }
    }

    override fun getViewModel() = OtherUsersViewModel::class.java

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentOtherUsersBinding.inflate(inflater, container, false)

    /**
     * Maybe show user profile, but most likely not needed with only follows.
     */
    override fun onOtherUserItemClicked(otherUser: OtherUser) {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mOtherUsersRecyclerAdapter.setLifecycleDestroyed()
    }

}