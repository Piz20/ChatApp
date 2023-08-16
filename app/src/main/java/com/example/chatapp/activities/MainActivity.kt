package com.example.chatapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import com.example.chatapp.fragments.ProfileFragment
import com.example.chatapp.R
import com.example.chatapp.fragments.TalksFragment
import com.example.chatapp.utils.FirebaseUtil
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : BaseActivity()  {
    lateinit var bottomNavigationView: BottomNavigationView
    lateinit var searchuserButton: ImageButton
    lateinit var deleteallchatroomsButton : ImageButton
    lateinit var talksFragment: TalksFragment
    lateinit var profileFragment: ProfileFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        talksFragment = TalksFragment()
        profileFragment = ProfileFragment()
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        searchuserButton = findViewById(R.id.person_search)
        deleteallchatroomsButton = findViewById(R.id.delete_all_chatrooms_button)
        setupUiConfiguration()

        getFCMToken()
    }

    //set the visibility of buttons search and delete for the current fragment
    fun setButtonVisibility(boolean: Boolean){
        if (boolean){
            searchuserButton.visibility = View.VISIBLE
            deleteallchatroomsButton.visibility = View.VISIBLE
        }else {
            searchuserButton.visibility=View.GONE
            deleteallchatroomsButton.visibility = View.GONE
        }
    }
    fun getFCMToken(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener{
            if(it.isSuccessful){
                  FirebaseUtil.currentUserDetails().update("fcmToken",it.result.toString())
            }
        }
    }

    fun setupUiConfiguration() {
        searchuserButton.setOnClickListener {
            startActivity(Intent(this, SearchUserActivity::class.java))
        }


        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_chat -> {
                    setButtonVisibility(true)
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frame_layout, talksFragment).commit()
                    true
                }

                R.id.menu_profile -> {
                    setButtonVisibility(false)
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frame_layout, profileFragment).commit()
                    true
                }

                else -> false
            }
        }
        bottomNavigationView.selectedItemId = R.id.menu_chat
    }





}