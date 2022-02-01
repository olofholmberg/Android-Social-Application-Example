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
import com.example.tddd80application.network.*
import com.example.tddd80application.responsedata.ApiMessage
import com.example.tddd80application.responsedata.OtherUser
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class OtherUserRecyclerAdapter(private var myDataSet: ArrayList<OtherUser>, private val otherUserItemClickListener: OnOtherUserItemClickListener) : RecyclerView.Adapter<OtherUserRecyclerAdapter.OtherUserViewHolder>() {

    private val otherUserViewHolders: MutableList<OtherUserViewHolder> = mutableListOf()

    class OtherUserViewHolder(view: View) : RecyclerView.ViewHolder(view), LifecycleOwner {

        private val lifecycleRegistry = LifecycleRegistry(this)
        private var wasPaused: Boolean = false

        private lateinit var otherUserData : OtherUser
        private val otherUserUsernameTextView: TextView = view.findViewById(R.id.recycler_other_user_view_username_text)
        private val otherUserFollowButton: MaterialButton = view.findViewById(R.id.recycler_other_user_view_follow_button)
        private var userIsFollowed: Boolean = false

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

        fun bind(otherUser: OtherUser, clickListener: OnOtherUserItemClickListener) {

            otherUserData = otherUser
            otherUserUsernameTextView.text = otherUser.username
            otherUserFollowButton.apply {
                setOnClickListener {
                    lifecycleScope.launch {
                        var userPreferences = UserPreferences(context)
                        val accessToken = userPreferences.accessToken.first()
                        if (accessToken != null && accessToken.isNotEmpty()) {
                            followSwap(accessToken, context)
                        }
                    }
                }
            }
            userIsFollowed = otherUser.is_followed == "True"
            updateFollowButtonTextAndIcon()

            itemView.setOnClickListener {
                clickListener.onOtherUserItemClicked(otherUser)
            }
        }

        private fun updateFollowButtonTextAndIcon() {
            if (userIsFollowed) {
                otherUserFollowButton.text = "Unfollow"
                otherUserFollowButton.setIconResource(R.drawable.ic_unfollow_user_24dp)
            } else {
                otherUserFollowButton.text = "Follow"
                otherUserFollowButton.setIconResource(R.drawable.ic_follow_user_24dp)
            }
        }

        /**
         * Sends the appropriate request to swap the follow status for the user that the
         * follow/unfollow button was clicked on.
         */
        private fun followSwap(accessToken: String, context: Context) {

            var followSwapListener = Response.Listener<ApiMessage> { response ->
                when(response.msg) {
                    "Follow successful" -> {
                        userIsFollowed = true
                    }
                    "Unfollow successful" -> {
                        userIsFollowed = false
                    }
                }
                updateFollowButtonTextAndIcon()
            }

            var followSwapErrorListener = Response.ErrorListener{ error ->
                if (hasApiErrorMessage(error)) {
                    Toast.makeText(context, getApiErrorMessage(error).msg , Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, getDefaultErrorMessage(error) , Toast.LENGTH_LONG).show()
                }
            }

            var headers = HashMap<String, String>()
            headers["Authorization"] = accessToken

            if (userIsFollowed) {
                var userUnfollowGsonRequest = GsonDeleteRequest<ApiMessage>(context.getString(R.string.base_url) + "followed_users/" + otherUserData.username,
                        ApiMessage::class.java, headers, followSwapListener, followSwapErrorListener)
                VolleySingleton.getInstance(context).addToRequestQueue(userUnfollowGsonRequest)
            } else {
                var userFollowGsonRequest = GsonPostRequest<ApiMessage>(context.getString(R.string.base_url) + "followed_users/" + otherUserData.username,
                        null ,ApiMessage::class.java, headers, followSwapListener, followSwapErrorListener)
                VolleySingleton.getInstance(context).addToRequestQueue(userFollowGsonRequest)
            }

        }

    }

    /** Invoked by the layout manager (creates new views) */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OtherUserViewHolder {
        val otherViewHolder = OtherUserViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recycler_other_user_view, parent, false))
        otherViewHolder.markCreated()
        otherUserViewHolders.add(otherViewHolder)
        return otherViewHolder
    }

    /** Invoked by the layout manager (replaces the contents of a view). */
    override fun onBindViewHolder(holder: OtherUserViewHolder, position: Int) {
        /**
         * Fetch the correct user from the data set based on the position and replace the
         * view contents based on the fetched user.
         */
        val otherUser = myDataSet[position]
        holder.bind(otherUser, otherUserItemClickListener)
    }

    fun updateData(data: ArrayList<OtherUser>) {
        myDataSet = data
        notifyDataSetChanged()
    }

    /** Invoked by the layout manager (should return size of the data set). */
    override fun getItemCount() = myDataSet.size

    /* - - - ViewHolder Lifecycle Management - - - */

    override fun onViewAttachedToWindow(holder: OtherUserViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.markAttach()
    }

    override fun onViewDetachedFromWindow(holder: OtherUserViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.markDetach()
    }

    fun setLifecycleDestroyed() {
        otherUserViewHolders.forEach {
            it.markDestroyed()
        }
    }

}

interface OnOtherUserItemClickListener {
    fun onOtherUserItemClicked(otherUser: OtherUser)
}



