package com.example.chatapp

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.chatapp.utils.FirebaseUtil
import com.google.android.material.snackbar.Snackbar
import com.vanniktech.emoji.EmojiEditText


class ProfileFragment : Fragment() {

    lateinit var usernameeditTextView: TextView
    lateinit var phonenumbereditTextView: TextView
    lateinit var editusername: ImageButton
    lateinit var editphonenumber: ImageButton
    lateinit var logoutButton: Button
    lateinit var progressBar: ProgressBar
    lateinit var sharedPreferences: SharedPreferences
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        usernameeditTextView = view.findViewById(R.id.username_field_edit)
        phonenumbereditTextView = view.findViewById(R.id.phone_number_field_edit)
        editusername = view.findViewById(R.id.ic_username_edit)
        editphonenumber = view.findViewById(R.id.ic_phone_number_edit)
        logoutButton = view.findViewById(R.id.button_logout)
        progressBar = view.findViewById(R.id.setinfo_pofile_progressbar)
        sharedPreferences =
            requireActivity().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        setupUi()
        return view
    }

    fun showSnackBar(msg: String) {
        Snackbar.make(
            requireActivity().findViewById(android.R.id.content),
            msg,
            Snackbar.LENGTH_SHORT
        ).show()
    }

    fun setupUi() {

        usernameeditTextView.text =
            sharedPreferences.getString("username", "")
        phonenumbereditTextView.text = sharedPreferences.getString("phonenumber", "")

        //Handle the display of username
        if (usernameeditTextView.text.isEmpty() || phonenumbereditTextView.text.isEmpty()) {
            setInProgress(true)
            FirebaseUtil.currentUserId()?.let {
                FirebaseUtil.usersCollection().document(it).get().addOnSuccessListener { task ->
                    if (task.exists()) {
                        usernameeditTextView.text = task.get("username").toString()
                        phonenumbereditTextView.text = task.get("phonenumber").toString()
                        setPreferences(
                            sharedPreferences,
                            task.get("username").toString(),
                            task.get("phonenumber").toString()
                        )
                        setInProgress(false)
                        println(
                            "*****************************************" + sharedPreferences.getString(
                                "username",
                                ""
                            )
                        )
                    }

                }
            }?.addOnFailureListener { task ->
                progressBar.visibility = View.GONE
                showSnackBar("We got a problem...")
            }
        }

        editusername.setOnClickListener {
            handleEditOfUsername()
        }
    }

    fun setPreferences(
        sharedPreferences: SharedPreferences,
        username: String,
        phonenumber: String? = sharedPreferences.getString("phonenumber", "")
    ) {
        val editor = sharedPreferences.edit()
        editor.putString(
            "username",
            username
        )
        editor.putString("phonenumber", phonenumber)
        editor.apply()
    }

    fun setInProgress(boolean: Boolean) {
        if (boolean)
            progressBar.visibility = View.VISIBLE
        else
            progressBar.visibility = View.GONE
    }

    //handle the modifying of username
    fun handleEditOfUsername() {
        val editText = EditText(requireContext())
        editText.setText(usernameeditTextView.text)
        editText.text.let { editText.setSelection(it.length) }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Edit your username")
            .setView(editText)
            .setPositiveButton("OK") { dialog, _ ->
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val editedText = editText.text.toString().trim()
                if (editedText.length >= 3) {
                    setInProgress(true)
                    FirebaseUtil.currentUserId()?.let {
                        FirebaseUtil.usersCollection().document(it)
                            .update("username", editedText)
                            .addOnSuccessListener {
                                setInProgress(false)
                                usernameeditTextView.text = editedText
                                setPreferences(sharedPreferences, editedText)
                                showSnackBar("That's how we gonna call you homie...")
                            }
                    }?.addOnFailureListener { showSnackBar("We got a problem") }
                    dialog.dismiss()
                } else {
                    editText.error = "Put at least 3 characters"
                }
            }
        }

        dialog.show()
    }
}
