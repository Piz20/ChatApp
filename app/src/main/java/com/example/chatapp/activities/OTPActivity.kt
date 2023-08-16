package com.example.chatapp.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.example.chatapp.R
import com.example.chatapp.models.User
import com.example.chatapp.utils.FirebaseUtil
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.Calendar
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit


class OTPActivity : BaseActivity() {
    lateinit var username: String
    lateinit var entercodeTextView: TextView
    lateinit var codeEditText: EditText
    lateinit var resendcodeTextView: TextView
    lateinit var phonenumber: String
    lateinit var progressBar: ProgressBar
    lateinit var verificationCode: String
    lateinit var resendingToken: PhoneAuthProvider.ForceResendingToken
    var timeoutSeconds: Long = 30L
    val mAuth: FirebaseAuth = FirebaseAuth.getInstance();
    lateinit var otpLayout: LinearLayout
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otpactivity)
        codeEditText = findViewById(R.id.code_field)
        resendcodeTextView = findViewById(R.id.resend_code_button)
        entercodeTextView = findViewById(R.id.txtENTERTHECODE)
        phonenumber = intent.getStringExtra("phonenumber").toString()
        username = intent.getStringExtra("username").toString()
        progressBar = findViewById(R.id.send_code_progressbar)
        otpLayout = findViewById(R.id.linearOTPActivity)
        sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        setupUi()
        sendOtp(phonenumber, false)
        verifyOTP(otpLayout)
        resendcodeTextView.setOnClickListener {
            sendOtp(phonenumber, true)
        }
    }

    //delete All sharedPreferences
    fun deleteAllSharedPreferences(){
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
    fun setPreferences(
        sharedPreferences: SharedPreferences, username: String="", phonenumber: String=""
    ) {
        val editor = sharedPreferences.edit()
        editor.putString(
            "username", username
        )
        editor.putString("phonenumber", phonenumber)
        editor.apply()
    }

    // To verify the OTP sent
    private fun verifyOTP(view: View) {
        codeEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {


            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun afterTextChanged(s: Editable?) {
                val otp = s.toString().length

                // Vérifier l'OTP ici
                if (otp == 6) {

                    val enteredCode: String = codeEditText.text.toString()
                    setInProgress(true)
                    try {
                        val credential: PhoneAuthCredential =
                            PhoneAuthProvider.getCredential(verificationCode, enteredCode)
                        signIn(credential)
                    } catch (e: UninitializedPropertyAccessException) {
                        setInProgress(false)
                        showSnackBar(
                            "Wrong validation code , try again later !"
                        )
                    }
                }

            }
        })
    }

    //  Set Basic Ui properties for the editTexts
    private fun setupUi() {
        codeEditText.gravity = Gravity.CENTER
        codeEditText.setSelection(codeEditText.text.length / 2)
        val intention: String? = intent.getStringExtra("intention")
        if (intention == null) {
            val message = getString(R.string.msg_enter_the_code) + phonenumber
            entercodeTextView.setText(message)
        } else if (intention == "delete") {
            val message =
                getString(R.string.msg_enter_the_code) + phonenumber + " TO DELETE THIS ACCOUNT"
            entercodeTextView.setText(message)
        } else if (intention == "updatephonenumber") {
            val message =
                getString(R.string.msg_enter_the_code) + phonenumber + " TO UPDATE YOUR PHONE NUMBER"
            entercodeTextView.setText(message)
        }
    }

    //function that handle the otp code sent to the user
    private fun sendOtp(phoneNumber: String, isResend: Boolean) {
        startResendTimer()
        setInProgress(true)
        val builder = PhoneAuthOptions.newBuilder(mAuth).setPhoneNumber(phoneNumber)
            .setTimeout(60, TimeUnit.SECONDS).setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                    setInProgress(false)
                }

                override fun onVerificationFailed(exception: FirebaseException) {
                    showSnackBar("Code verification failed , try again later").show()
                    setInProgress(false)
                }

                override fun onCodeSent(
                    verifcationId: String,
                    forceResengdingToken: PhoneAuthProvider.ForceResendingToken
                ) {
                    super.onCodeSent(verifcationId, forceResengdingToken)
                    verificationCode = verifcationId
                    resendingToken = forceResengdingToken
                    showSnackBar("Code sent succesfully").show()
                    setInProgress(false)

                }

            })
        try {
            if (isResend) {

                PhoneAuthProvider.verifyPhoneNumber(
                    builder.setForceResendingToken(resendingToken).build()
                )
            } else {

                PhoneAuthProvider.verifyPhoneNumber(builder.build())
            }
        } catch (e: UninitializedPropertyAccessException) {
            recreate()
        }
    }


    //To control the visibility of the progreesBar
    fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            progressBar.visibility = View.VISIBLE

        } else {
            progressBar.visibility = View.GONE
        }
    }

    //To handle the Signin or the Signup of the user
    @RequiresApi(Build.VERSION_CODES.O)
    private fun signIn(phoneAuthCredential: PhoneAuthCredential) {
        setInProgress(true)

        val intention = intent.getStringExtra("intention")
        //If this activity was trigerred to update phoneneumber
        if (intention == "updatephonenumber")
            FirebaseAuth.getInstance().currentUser!!.unlink(PhoneAuthProvider.PROVIDER_ID).addOnSuccessListener {
                FirebaseAuth.getInstance().currentUser!!.linkWithCredential(phoneAuthCredential).addOnSuccessListener {
                    FirebaseUtil.currentUserDetails()
                        .update("phonenumber", phonenumber)
                        .addOnSuccessListener {
                            setInProgress(false)
                            showSnackBar(
                                "Phone number updated succesfully"
                            ).addCallback(object : Snackbar.Callback() {
                                override fun onDismissed(
                                    transientBottomBar: Snackbar?, event: Int
                                ) {
                                    startMainActivity()
                                }
                            }).show()
                            setPreferences(sharedPreferences,phonenumber=phonenumber)
                        }
                        .addOnFailureListener {
                            println(it.message)
                            showSnackBar(
                                "We got a problem"
                            ).addCallback(object : Snackbar.Callback() {
                                override fun onDismissed(
                                    transientBottomBar: Snackbar?, event: Int
                                ) {
                                    startMainActivity()
                                }
                            }).show()
                        }

                }.addOnFailureListener {
                    println(it.message)
                    showSnackBar(
                        "We got a problem"
                    ).addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(
                            transientBottomBar: Snackbar?, event: Int
                        ) {
                            startMainActivity()
                        }
                    }).show()
                }
            }.addOnFailureListener{
                println(it.message)
                showSnackBar(
                    "We got a problem"
                ).addCallback(object : Snackbar.Callback() {
                    override fun onDismissed(
                        transientBottomBar: Snackbar?, event: Int
                    ) {
                        startMainActivity()
                    }
                }).show()
            }

        //If this activity was trigerred to delete an account
        if (intention == "delete") {
            FirebaseUtil.reauthenticate(phoneAuthCredential).addOnSuccessListener {
                FirebaseUtil.currentUserDetails().delete()
                    .addOnSuccessListener {
                        FirebaseAuth.getInstance().currentUser!!.delete().addOnSuccessListener {
                            setInProgress(false)
                            deleteAllSharedPreferences()
                            showSnackBar(
                                "Farewell..."
                            ).addCallback(object : Snackbar.Callback() {
                                override fun onDismissed(
                                    transientBottomBar: Snackbar?, event: Int
                                ) {
                                    startSignupActivity()
                                }
                            }).show()
                        }.addOnFailureListener {
                            showSnackBar("We got a problem").show()
                            println(it.message)
                            setInProgress(false)
                        }
                    }.addOnFailureListener {
                        showSnackBar("We got a problem").show()
                        println(it.message)
                        setInProgress(false)
                    }
            }.addOnFailureListener {
                showSnackBar("We got a problem").show()
                println(it.message)
                setInProgress(false)
            }
        }
        //If this activity was trigerred To login or signup
        if (intention == null) {
            FirebaseUtil.signIn(phoneAuthCredential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //if this user already exists we just sign in
                    FirebaseUtil.usersCollection().whereEqualTo("phonenumber", phonenumber).get()
                        .addOnSuccessListener {
                            if (!it.isEmpty) {
                                        setInProgress(false)
                                        showSnackBar(
                                            "Welcome back homie..."
                                        ).addCallback(object : Snackbar.Callback() {
                                            override fun onDismissed(
                                                transientBottomBar: Snackbar?, event: Int
                                            ) {
                                                startMainActivity()
                                            }
                                        }).show()
                                        setPreferences(sharedPreferences, username, phonenumber)


                            } else {
                                // if that user does not exists we creat him
                                val user = FirebaseUtil.currentUserId()?.let {
                                    User(
                                        it,
                                        username,
                                        phonenumber,
                                        Calendar.getInstance().time,
                                    )
                                }

                                if (user != null) {
                                    FirebaseUtil.currentUserDetails().set(user)
                                        .addOnSuccessListener {
                                            setInProgress(false)
                                            showSnackBar(
                                                "Welcome homie..."
                                            ).addCallback(object : Snackbar.Callback() {
                                                override fun onDismissed(
                                                    transientBottomBar: Snackbar?, event: Int
                                                ) {
                                                    startMainActivity()
                                                }
                                            }).show()
                                            setPreferences(sharedPreferences, username, phonenumber)

                                        }.addOnFailureListener { exception ->
                                            print(exception)
                                            setInProgress(false)
                                            showSnackBar(
                                                "We got a problem..."
                                            ).addCallback(object : Snackbar.Callback() {
                                                override fun onDismissed(
                                                    transientBottomBar: Snackbar?, event: Int
                                                ) {
                                                }
                                            }).show()


                                        }
                                }


                            }
                        }.addOnFailureListener {
                            showSnackBar("We got a problem...").show()
                        }


                } else {
                    setInProgress(false)
                    showSnackBar("Wrong validation code!").show()
                    val exception = task.exception
                    Log.e("Authentification", "Error: ${exception?.message}")
                }
            }
        }
    }

    // To handle the resend code button
    private fun startResendTimer() {
        resendcodeTextView.isEnabled = false
        val timer = Timer()

        val task = object : TimerTask() {
            override fun run() {
                timeoutSeconds--;
                runOnUiThread {
                    resendcodeTextView.setText("RESEND CODE IN " + timeoutSeconds + " SECOND(S)")
                }
                if (timeoutSeconds <= 0) {
                    timeoutSeconds = 30L
                    timer.cancel()
                    runOnUiThread {
                        resendcodeTextView.isEnabled = true
                        resendcodeTextView.setText("OR RESEND THE CODE")
                    }
                }
            }

        }

        timer.scheduleAtFixedRate(task, 0, 1000)
    }

    //To launch our MainActivity
    fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun startSignupActivity() {
        val intent = Intent(this, SignupActivity::class.java)
        startActivity(intent)
        finish()
    }
}
