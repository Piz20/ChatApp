package com.example.chatapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.chatapp.utils.FirebaseUtil
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : BaseActivity() {
    lateinit var bottomNavigationView: BottomNavigationView
    lateinit var searchuserButton: ImageButton
    lateinit var talksFragment: TalksFragment
    lateinit var profileFragment: ProfileFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        talksFragment = TalksFragment()
        profileFragment = ProfileFragment()
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        searchuserButton = findViewById(R.id.person_search)
        setupUiConfiguration()
    }

    fun setupUiConfiguration() {
        searchuserButton.setOnClickListener {
            startActivity(Intent(this, SearchUserActivity::class.java))
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_chat -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frame_layout, talksFragment).commit()
                    true
                }

                R.id.menu_profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frame_layout, profileFragment).commit()
                    true
                }

                else -> false
            }
        }
        bottomNavigationView.selectedItemId = R.id.menu_chat
    }


    fun startSignupActivity() {
        val intent = Intent(this, SignupActivity::class.java)
        startActivity(intent)
        finish()
    }
}