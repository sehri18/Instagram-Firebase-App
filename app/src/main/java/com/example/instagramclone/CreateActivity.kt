package com.example.instagramclone

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.instagramclone.models.Post
import com.example.instagramclone.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

private const val TAG = "CreateActivity"
private const val PICK_PHOTO_CODE = 1234

class CreateActivity : AppCompatActivity() {


    private var photoUri : Uri? = null
    private var signedInUser: User? = null
    private lateinit var firestoreDb : FirebaseFirestore
    private lateinit var storageReference: StorageReference

    private lateinit var  btnPickImage : Button
    private lateinit var  btnSubmit : Button
    private lateinit var  etDescription : EditText
    private lateinit var  imageView : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        btnPickImage = findViewById(R.id.btnPickImage)
        btnSubmit = findViewById(R.id.btnSubmit)

        storageReference = FirebaseStorage.getInstance().reference
        firestoreDb = FirebaseFirestore.getInstance()

        firestoreDb.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid as String)
            .get()
            .addOnSuccessListener { userSnapshot ->
                signedInUser = userSnapshot.toObject(User::class.java)
                Log.i(TAG, "signed in user: $signedInUser")
            }
            .addOnFailureListener { exception ->
                Log.i(TAG, "Failure fetching signed in user", exception)
            }


        btnPickImage.setOnClickListener{
            Log.i(TAG, "Open image picker on device")
            val imagePickerIntent = Intent(Intent.ACTION_GET_CONTENT)
            imagePickerIntent.type = "image/*"
            if(imagePickerIntent.resolveActivity(packageManager) != null){
                startActivityForResult(imagePickerIntent, PICK_PHOTO_CODE)
            }
        }

        btnSubmit.setOnClickListener{
            handleSubmitButtonClick()
        }    }

    private fun handleSubmitButtonClick() {
        etDescription = findViewById(R.id.etDescription)
        imageView = findViewById(R.id.imageView)
        btnSubmit = findViewById(R.id.btnSubmit)


        if(photoUri == null){
            Toast.makeText(this, "No photo selected", Toast.LENGTH_SHORT).show()
            return
        }
        if(etDescription.text.isBlank()) {
            Toast.makeText(this, "Description cannot be empty", Toast.LENGTH_SHORT).show()
        }
        if(signedInUser ==  null){
            Toast.makeText(this, "No signed in user, please wait", Toast.LENGTH_SHORT).show()
            return
        }

        btnSubmit.isEnabled = false

        val photoUploadUri = photoUri as Uri
        val photoReference = storageReference.child("images/${System.currentTimeMillis()}-photo.jpg")
        //Upload image to firebase storage
        photoReference.putFile(photoUploadUri)
            .continueWithTask { photoUploadTask ->
                Log.i(TAG,"uploaded bytes: ${photoUploadTask.result?.bytesTransferred}")

                //Retrieve image url of the uploaded image
                photoReference.downloadUrl
            }.continueWithTask { downloadUrlTask ->
                //Create a post object with the image URL and add that to the posts collection in firestore
                val post = Post(
                    etDescription.text.toString(),
                    downloadUrlTask.result.toString(),
                    System.currentTimeMillis(),
                    signedInUser)
                firestoreDb.collection("posts").add(post)
            }.addOnCompleteListener { postCreationTask ->
                btnSubmit.isEnabled = true
                if(!postCreationTask.isSuccessful){
                    Log.e(TAG, "Exception during firebase operations", postCreationTask.exception)
                    Toast.makeText(this, "Failed to save post", Toast.LENGTH_SHORT).show()
                }
                etDescription.text.clear()
                imageView.setImageResource(0)
                Toast.makeText(this, "Success!",Toast.LENGTH_SHORT).show()
                val profileIntent = Intent(this, ProfileActivity::class.java)
                profileIntent.putExtra(EXTRA_USERNAME,signedInUser?.username)
                startActivity(profileIntent)
                finish()
            }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val imageView = findViewById<ImageView>(R.id.imageView)

        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_PHOTO_CODE){
            if(resultCode == Activity.RESULT_OK){
                photoUri = data?.data
                Log.i(TAG, "photoUri: $photoUri")
                imageView.setImageURI(photoUri)
            }
            else{
                Toast.makeText(this, "Image picker action canceled",Toast.LENGTH_SHORT).show()
            }
        }


    }
}