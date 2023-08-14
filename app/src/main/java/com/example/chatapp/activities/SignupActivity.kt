package com.example.chatapp.activities

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.example.chatapp.R
import com.example.chatapp.utils.FirebaseUtil
import com.google.firebase.FirebaseApp
import com.hbb20.CountryCodePicker


class SignupActivity : BaseActivity() {

    lateinit var usernameEditText: EditText
    lateinit var phonenumberEditText: EditText
    lateinit var countrycodePicker: CountryCodePicker
    lateinit var signupButton: Button
    lateinit var loginButton: Button
    lateinit var progressBar: ProgressBar
    lateinit var signupLinearLayout: LinearLayout
    private var selectedButton: Button? = null
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signupactivity)
        FirebaseApp.initializeApp(applicationContext)
        usernameEditText = findViewById(R.id.username_field)
        phonenumberEditText = findViewById(R.id.phone_number_field)
        countrycodePicker = findViewById(R.id.countryCodePicker)
        signupButton = findViewById(R.id.button_signup)
        progressBar = findViewById(R.id.registration_progressbar)
        loginButton = findViewById(R.id.button_login)
        countrycodePicker.registerCarrierNumberEditText(phonenumberEditText)
        signupLinearLayout = findViewById(R.id.linearSignUpActivity)
        setupUi()
        setInProgress(false)
    }

    //  Set Basic Ui properties for the editTexts
    private fun setupUi() {
        usernameEditText.gravity = Gravity.CENTER
        usernameEditText.setSelection(usernameEditText.text.length / 2)

        phonenumberEditText.gravity = Gravity.CENTER
        phonenumberEditText.setSelection(phonenumberEditText.text.length / 2)

        signupLinearLayout.setOnClickListener {
            resetButtonState(signupButton, 0)
            resetButtonState(loginButton, 0)
        }

    }

    fun resetButtonState(button: Button, delay: Long) {
        handler.postDelayed({
            button.setBackgroundResource(R.drawable.button_background)
            button.setTextColor(getColor(R.color.white))
        }, delay)
    }

    //handle the click on differents buttons

    fun onButtonClick(view: View) {

        // re-initialize the state of the previous clicked button
        selectedButton?.setBackgroundResource(R.drawable.button_background)
        selectedButton?.setTextColor(getColor(R.color.white))
        // Update the actually selected Button
        selectedButton = view as Button

        // Change Color of clicked button
        view.setBackgroundResource(R.drawable.button_pressed_background)
        view.setTextColor(getColor(R.color.black))

        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo

        // Handle Signup UI Side
        if (view == signupButton) {
            if (networkInfo != null && networkInfo.isConnected) {
                setInProgress(true)
                if (!countrycodePicker.isValidFullNumber) {
                    setInProgress(false)
                    phonenumberEditText.error = "Phone number not valid !"
                    resetButtonState(signupButton, 1000)
                }
                if (usernameEditText.text.length < 3) {
                    setInProgress(false)
                    usernameEditText.error = "The username must have at least 3 characters !"
                    resetButtonState(signupButton, 1000)

                } else if (countrycodePicker.isValidFullNumber && usernameEditText.text.length >= 3) {
                    FirebaseUtil.usersCollection()
                        .whereEqualTo("username", usernameEditText.text.toString()).get()
                        .addOnSuccessListener {
                            if (!it.isEmpty) {
                                setInProgress(false)
                                showSnackBar(
                                    "Somebody is already called like this..."
                                ).show()
                                resetButtonState(signupButton, 1000)
                            } else {
                                FirebaseUtil.usersCollection().whereEqualTo(
                                    "phonenumber",
                                    countrycodePicker.fullNumberWithPlus.toString()
                                ).get()
                                    .addOnSuccessListener {
                                        if (!it.isEmpty) {
                                            setInProgress(false)
                                            showSnackBar(
                                                "Seems like you already took a journey here , login instead..."
                                            ).show()
                                            resetButtonState(signupButton, 1000)
                                        } else {
                                            setInProgress(false)
                                            startOTPActivity()
                                            resetButtonState(signupButton, 1000)
                                        }

                                    }
                                    .addOnFailureListener {
                                        setInProgress(false)
                                        showSnackBar("We got a problem").show()
                                        resetButtonState(signupButton, 1000)

                                    }
                            }
                        }.addOnFailureListener{
                            setInProgress(false)
                            showSnackBar("We got a problem").show()
                            resetButtonState(signupButton, 1000)
                        }
                }


            } else {
                showSnackBar("Come on enable your datas or the WI-FI").show()
                resetButtonState(signupButton, 1000)

            }
        }
        if (view == loginButton) {
            if (networkInfo != null && networkInfo.isConnected) {
                setInProgress(true)
                usernameEditText.setText("")
                if (!countrycodePicker.isValidFullNumber) {
                    setInProgress(false)
                    phonenumberEditText.error = "Phone number not valid !"
                    resetButtonState(loginButton, 1000)
                } else if (countrycodePicker.isValidFullNumber) {
                    FirebaseUtil.usersCollection().whereEqualTo(
                        "phonenumber",
                        countrycodePicker.fullNumberWithPlus.toString()
                    ).get()
                        .addOnSuccessListener {
                            if (!it.isEmpty) {
                                usernameEditText.setText(it.documents[0].get("username").toString())
                                setInProgress(false)
                                startOTPActivity()
                            } else {
                                setInProgress(false)
                                showSnackBar(
                                    "Seems like you never been here before , signup instead... "
                                ).show()
                                resetButtonState(loginButton, 1000)

                            }
                        }
                        .addOnFailureListener {
                            setInProgress(false)
                            showSnackBar("We got a problem").show()
                            resetButtonState(loginButton, 1000)

                        }
                }
            } else {
                showSnackBar("Come on enable your datas or the WI-FI").show()
                resetButtonState(loginButton, 1000)

            }
        }


    }


    private fun startOTPActivity() {
        val intent = Intent(this, OTPActivity::class.java)
        intent.putExtra("phonenumber", countrycodePicker.fullNumberWithPlus.toString())
        intent.putExtra("username", usernameEditText.text.toString().trim())
        startActivity(intent)
    }

    //To control the visibility of the progreesBar
    fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            progressBar.visibility = View.VISIBLE

        } else {
            progressBar.visibility = View.GONE


        }
    }
}
