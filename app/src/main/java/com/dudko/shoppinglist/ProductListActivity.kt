package com.dudko.shoppinglist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.FirebaseDatabase

//import com.google.firebase.database.FirebaseDatabase


class ProductListActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.shopping_list)

        val database = FirebaseDatabase.getInstance()
        val userId = mAuth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "You need to sign in again", Toast.LENGTH_SHORT).show()
            return
        }

        val userListRef = database.getReference(userId)

        val rv = findViewById<RecyclerView>(R.id.shopping_list)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = ShoppingListAdapter(this, userListRef)

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            val intent = Intent(this, EditProductActivity::class.java)
            startActivity(intent)
        }
    }
}