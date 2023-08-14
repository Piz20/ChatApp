package com.example.chatapp.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.chatapp.R
import com.example.chatapp.activities.OTPActivity
import com.example.chatapp.activities.SignupActivity
import com.example.chatapp.utils.AndroidUtil
import com.example.chatapp.utils.FirebaseUtil
import com.github.chrisbanes.photoview.PhotoView
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging
import com.hbb20.CountryCodePicker
import java.io.File


class ProfileFragment : Fragment() {
    lateinit var usernameeditTextView: TextView
    lateinit var phonenumbereditTextView: TextView
    lateinit var logoutButton: Button
    lateinit var deleteAccountButton: Button
    lateinit var editprofileimage: ImageView
    lateinit var sharedPreferences: SharedPreferences
    lateinit var profileRelativeLayout: RelativeLayout
    lateinit var progressBar: ProgressBar
    lateinit var selectedImageUri: Uri
    lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    lateinit var fullScreenImageView : PhotoView
    lateinit var fullScreenImageProgressBar: ProgressBar
    //Handle the actions when profile picture is selected

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imagePickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    selectedImageUri = result.data?.data!!
                    setInProgress(true)
                    setFullScreenInProgress(true)
                    FirebaseUtil.getCurrentUserProfilePicStorageReference()
                        ?.putFile(selectedImageUri)
                        ?.addOnSuccessListener {
                            setInProgress(false)
                            setFullScreenInProgress(false)
                            showSnackBar("So got a new face ?")
                            AndroidUtil.setProfilePic(
                                requireContext(),
                                selectedImageUri,
                                editprofileimage
                            )
                            setImageInFullScreenImage()
                            setPreferences(
                                sharedPreferences,
                                profilepicstring = selectedImageUri.toString()
                            )
                            deleteFilesInDcimDirectory()
                        }?.addOnFailureListener {
                            setInProgress(false)
                            setFullScreenInProgress(false)
                            showSnackBar("We got a prolem...")
                        }
                }
            }
    }

    private fun deleteFilesInDcimDirectory() {
        val dcimDirectory = File(requireContext().getExternalFilesDir(null), "DCIM")
        if (dcimDirectory.exists() && dcimDirectory.isDirectory) {
            val files = dcimDirectory.listFiles()
            if (files != null && files.size > 2) {
                // Triez les fichiers par date de modification (le plus récent en premier)
                files.sortByDescending { it.lastModified() }

                // Conservez les deux derniers fichiers
                val filesToKeep = files.take(2)

                // Supprimez les autres fichiers
                for (file in files) {
                    if (file !in filesToKeep) {
                        file.delete()
                    }
                }
            }
        }
    }

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        usernameeditTextView = view.findViewById(R.id.username_field_edit)
        phonenumbereditTextView = view.findViewById(R.id.phone_number_field_edit)
        logoutButton = view.findViewById(R.id.button_logout)
        deleteAccountButton = view.findViewById(R.id.button_delete_account)
        editprofileimage = view.findViewById(R.id.edit_profile_image)
        profileRelativeLayout = view.findViewById(R.id.profile_relativeLayout)
        progressBar = view.findViewById(R.id.setinfo_pofile_progressbar)
        sharedPreferences =
            requireActivity().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        setupUi()
        return view
    }

    fun resetButtonsState() {
        logoutButton.setBackgroundResource(R.drawable.button_background)
        logoutButton.setTextColor(resources.getColor(R.color.white))
        deleteAccountButton.setBackgroundResource(R.drawable.button_background)
        deleteAccountButton.setTextColor(resources.getColor(R.color.white))
    }

    //like his name useful to showsnackbar
    fun showSnackBar(msg: String) {
        Snackbar.make(
            requireActivity().findViewById(android.R.id.content),
            msg,
            Snackbar.LENGTH_SHORT
        ).show()
    }

    //delete All sharedPreferences
    fun deleteAllSharedPreferences() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    //to set preferences
    fun setPreferences(
        sharedPreferences: SharedPreferences,
        username: String? = sharedPreferences.getString("username", ""),
        phonenumber: String? = sharedPreferences.getString("phonenumber", ""),
        profilepicstring: String? = sharedPreferences.getString("profilepicstring", "")
    ) {
        val editor = sharedPreferences.edit()
        editor.putString(
            "username",
            username
        )
        editor.putString("phonenumber", phonenumber)
        editor.apply()

        editor.putString("profilepicstring", profilepicstring)
        editor.apply()
    }

    fun setInProgress(boolean: Boolean) {
        if (boolean)
            progressBar.visibility = View.VISIBLE
        else
            progressBar.visibility = View.GONE
    }

    //Set the image in the fullScreen
    fun setImageInFullScreenImage(){
        Glide.with(this).load(selectedImageUri).error(R.drawable.icon_user_white).into(fullScreenImageView)
    }

    //handle the fullscreenprogressbar
    fun setFullScreenInProgress(boolean: Boolean){
        if(boolean){
            fullScreenImageView.visibility =View.GONE
            fullScreenImageProgressBar.visibility = View.VISIBLE
        }else{
            fullScreenImageProgressBar.visibility = View.GONE
            fullScreenImageView.visibility = View.VISIBLE
        }
    }

    fun showFullScreenImage() {
       //Create a new dialog for the image
        val dialog = Dialog(requireContext(), R.style.AppTheme_NoActionBar)
        dialog.setContentView(R.layout.dialog_fullscreen_image)

        // Initialize photoview and buttons
        fullScreenImageView = dialog.findViewById(R.id.fullscreen_content)
        val buttonBack = dialog.findViewById<ImageButton>(R.id.back_button)
        val buttonEdit = dialog.findViewById<ImageButton>(R.id.edit_profile_picture)
        val buttonDelete = dialog.findViewById<ImageButton>(R.id.delete_profile_picture)
        fullScreenImageProgressBar = dialog.findViewById(R.id.fullscreenimage_progressbar)
        selectedImageUri = Uri.parse(sharedPreferences.getString("profilepicstring", ""))
        setImageInFullScreenImage()
        fun showDeleteConfirmationDialog() {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Delete this pic")
                .setMessage("Don't you want put a face to your name?")
                .setPositiveButton("Of Course") { dialog, _ ->
                    setInProgress(true)
                    setFullScreenInProgress(true)
                    FirebaseUtil.getCurrentUserProfilePicStorageReference()?.delete()
                        ?.addOnSuccessListener {
                            setPreferences(sharedPreferences, profilepicstring = "delete")
                            AndroidUtil.setProfilePic(requireContext(),Uri.parse(""),editprofileimage)
                            setInProgress(false)
                            setFullScreenInProgress(false)
                            Glide.with(this).load("").error(R.drawable.icon_user_white).into(fullScreenImageView)
                            showSnackBar("So you are Sin Cara ?")
                        }
                        ?.addOnFailureListener{
                          setInProgress(false)
                            setFullScreenInProgress(false)
                            showSnackBar("We got a problem")
                        }
                    dialog.dismiss()
                }
                .setNegativeButton("Noooo") { dialog, _ ->
                    dialog.dismiss()
                }

            val alertDialog = builder.create()
            alertDialog.show()
        }

        buttonBack.setOnClickListener {
            dialog.dismiss()
        }
        buttonEdit.setOnClickListener {
            ImagePicker.with(this)
                .cropSquare()
                .compress(512)         //Final image size will be less than 1 MB(Optional)
                .maxResultSize(
                    512,
                    512
                )  //Final image resolution will be less than 1080 x 1080(Optional)
                .createIntent {
                    imagePickerLauncher.launch(it)
                }

        }
        buttonDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        // Affichez le dialogue
        dialog.show()
    }

    fun setupUi() {

        //handle profile picture change
        editprofileimage.setOnClickListener {
            showFullScreenImage()
        }

        //set the text for the usernmae and for the phonenumber and the profile picture
        usernameeditTextView.text =
            sharedPreferences.getString("username", "")
        phonenumbereditTextView.text = sharedPreferences.getString("phonenumber", "")
        AndroidUtil.setProfilePic(
            requireContext(),
            Uri.parse(sharedPreferences.getString("profilepicstring", "")),
            editprofileimage
        )


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
                    }

                }
            }?.addOnFailureListener { task ->
                setInProgress(false)
                showSnackBar("We got a problem...")
            }
        }

        //handle the display of profile picture when there is nothing in the profile picture directory
        if (sharedPreferences.getString("profilepicstring", "") == "") {
            setInProgress(true)
            val dcimDirectory = File(requireContext().getExternalFilesDir(null), "DCIM")
            if (!dcimDirectory.exists()) {
                dcimDirectory.mkdirs()
            }
            val localFile = File(dcimDirectory, "profile_pic.jpg")

            FirebaseUtil.getCurrentUserProfilePicStorageReference()?.getFile(localFile)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        setInProgress(false)
                        val profilepictureUri = Uri.fromFile(localFile)
                        setPreferences(
                            sharedPreferences,
                            profilepicstring = profilepictureUri.toString()
                        )
                        AndroidUtil.setProfilePic(
                            requireContext(),
                            Uri.parse(sharedPreferences.getString("profilepicstring", null)),
                            editprofileimage
                        )
                        if (dcimDirectory.exists() && dcimDirectory.listFiles()?.size!! > 1) {
                            dcimDirectory.listFiles()
                                ?.sortByDescending { it.lastModified() }
                            for (file in dcimDirectory.listFiles()!!.dropLast(1))
                                file.delete()
                        }

                    }
                }?.addOnFailureListener {
                    setInProgress(false)
                    showSnackBar("No Face Homie...")
                }
        }

        usernameeditTextView.setOnClickListener {
            handleEditOfUsername()
        }

        phonenumbereditTextView.setOnClickListener {
            handleEditOfPhoneNumber()
        }

        onButtonLogoutClicked()
        onButtonDeleteClicked()
    }

    fun onButtonLogoutClicked() {
        logoutButton.setOnClickListener {

            logoutButton.setBackgroundResource(R.drawable.button_pressed_background)
            logoutButton.setTextColor(resources.getColor(R.color.black))
            deleteAccountButton.setBackgroundResource(R.drawable.button_background)
            deleteAccountButton.setTextColor(resources.getColor(R.color.white))
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Do you really want to leave ?")
                .setPositiveButton("OK") { dialog, _ ->
                    FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener{
                        if(it.isSuccessful){
                            FirebaseUtil.signOut()
                            deleteAllSharedPreferences()
                            showSnackBar("Goodbye homie...")
                            val intent = Intent(requireContext(), SignupActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                            requireActivity().finish()
                        }
                    }

                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    showSnackBar("Cool , you stay finally .")
                    dialog.dismiss()
                    logoutButton.setBackgroundResource(R.drawable.button_background)
                    logoutButton.setTextColor(resources.getColor(R.color.white))
                }
                .setOnCancelListener {
                    resetButtonsState()
                }
                .create()
            dialog.show()

        }
    }

    fun onButtonDeleteClicked() {
        deleteAccountButton.setOnClickListener {
            deleteAccountButton.setBackgroundResource(R.drawable.button_pressed_background)
            deleteAccountButton.setTextColor(resources.getColor(R.color.black))
            logoutButton.setBackgroundResource(R.drawable.button_background)
            logoutButton.setTextColor(resources.getColor(R.color.white))
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("So that's the end homie?")
                .setPositiveButton("OK") { dialog, _ ->
                    setInProgress(true)
                    val intent = Intent(requireContext(), OTPActivity::class.java)
                    intent.putExtra("intention", "delete")
                    intent.putExtra("phonenumber", phonenumbereditTextView.text.toString())
                    startActivity(intent)
                    requireActivity().finish()
                }

                .setNegativeButton("Cancel") { dialog, _ ->
                    showSnackBar("We still have a long way to go...")
                    dialog.dismiss()
                    deleteAccountButton.setBackgroundResource(R.drawable.button_background)
                    deleteAccountButton.setTextColor(resources.getColor(R.color.white))
                }
                .setOnCancelListener {
                    resetButtonsState()
                }
                .create()
            dialog.show()

        }
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
                    FirebaseUtil.usersCollection().whereEqualTo("username",editedText).get().addOnSuccessListener {
                        if(it.isEmpty){
                            FirebaseUtil.currentUserDetails()
                                .update("username", editedText)
                                .addOnSuccessListener {
                                    setInProgress(false)
                                    usernameeditTextView.text = editedText
                                    setPreferences(sharedPreferences, editedText)
                                    showSnackBar("That's how we gonna call you homie...")
                                }
                                .addOnFailureListener {
                                    showSnackBar("We got a problem")
                                    setInProgress(false)
                                }
                            dialog.dismiss()
                        }
                        else{
                            editText.error = "This username is already used."
                        }
                    }.addOnFailureListener{
                        setInProgress(false)
                        showSnackBar("We got a problem...")
                    }

                } else {
                    editText.error = "Put at least 3 characters"
                }
            }
        }

        dialog.show()
    }

    //To handle the edit of phonenumber
    fun handleEditOfPhoneNumber() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_custom_layout, null)
        val countryCodePicker = dialogView.findViewById<CountryCodePicker>(R.id.countryCodePicker)
        val editText = dialogView.findViewById<EditText>(R.id.editText)

        editText.gravity = Gravity.CENTER
        editText.setSelection(editText.length() / 2)
        editText.inputType = InputType.TYPE_CLASS_PHONE
        editText.maxLines = 1
        countryCodePicker.registerCarrierNumberEditText(editText)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Edit your phone number")
            .setView(dialogView)
            .setPositiveButton("OK") { dialog, _ ->
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val editedText = countryCodePicker.fullNumberWithPlus

                if (countryCodePicker.isValidFullNumber && editedText != sharedPreferences.getString(
                        "phonenumber",
                        ""
                    )
                ) {
                    setInProgress(true)
                    FirebaseUtil.usersCollection().whereEqualTo("phonenumber", editedText).get()
                        .addOnSuccessListener { task ->
                            if (!task.isEmpty) {
                                editText.error = "This phonenumber  is already used"
                                setInProgress(false)
                            } else {
                                setInProgress(false)
                                val intent = Intent(requireContext(), OTPActivity::class.java)
                                intent.putExtra(
                                    "username",
                                    sharedPreferences.getString("username", "")
                                )
                                intent.putExtra("phonenumber", editedText)
                                intent.putExtra("intention", "updatephonenumber")
                                startActivity(intent)
                                requireActivity().finish()
                                dialog.dismiss()

                            }
                        }.addOnFailureListener {
                            showSnackBar("We got a problem...")
                            setInProgress(false)
                        }

                } else if (editedText == sharedPreferences.getString(
                        "phonenumber",
                        ""
                    )
                ) {
                    showSnackBar("Same phonenumber , nothing changed...")
                } else if (!countryCodePicker.isValidFullNumber) {
                    editText.error = "Invalid format for the phonenumber"
                }

            }
        }
        dialog.show()
    }


}
