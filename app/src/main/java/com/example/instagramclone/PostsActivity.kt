package com.example.instagramclone

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.models.Post
import com.example.instagramclone.models.User
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

private const val TAG = "PostsActivity"
const val EXTRA_USERNAME = "EXTRA_USERNAME"

open class PostsActivity : AppCompatActivity() {

    private var signedInUser: User? = null
    private lateinit var firestoreDb : FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posts)

        val fabCreate : FloatingActionButton = findViewById(R.id.fabCreate)

        //1. Create the layout file that represents a single post - DONE
        //2. Create data source
        val posts: MutableList<Post> = mutableListOf()

        //3. Create the adapter
        val adapter: PostsAdapter = PostsAdapter(this, posts)

        //4. Bind the adapter and layout manager to the RecyclerView
        val rvPosts = findViewById<RecyclerView>(R.id.rvPosts)
        rvPosts.adapter = adapter

        rvPosts.layoutManager = LinearLayoutManager(this)

        // Query to Firestore to retrieve data
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


        var postsReference = firestoreDb
            .collection("posts")
            .limit(20)
            .orderBy("creation_time_ms", Query.Direction.DESCENDING)

        val username= intent.getStringExtra(EXTRA_USERNAME)
        if(username != null)
        {
            supportActionBar?.title = username
            postsReference = postsReference.whereEqualTo("user.username", username )
        }

        postsReference.addSnapshotListener{snapshot, exception ->
            if(exception !=null || snapshot == null){
                Log.e(TAG, "Exception when querying posts", exception)
                return@addSnapshotListener
            }
            val postList = snapshot.toObjects(Post :: class.java)

            posts.clear()
            posts.addAll(postList)
            adapter.notifyDataSetChanged()

            for (post in postList){
                Log.i(TAG, "Post: ${post}")
            }

        }

    fabCreate.setOnClickListener {
        val intent = Intent( this, CreateActivity:: class.java)
        startActivity(intent)
    }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_posts, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.menu_profile){
            val intent = Intent(this, ProfileActivity :: class.java)
            intent.putExtra(EXTRA_USERNAME, signedInUser?.username)
            startActivity(intent)

        }
        return super.onOptionsItemSelected(item)
    }
}