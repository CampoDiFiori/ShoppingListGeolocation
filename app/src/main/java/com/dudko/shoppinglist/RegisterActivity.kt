package com.dudko.shoppinglist

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast

class RegisterActivity() : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_activity)
    }

    fun onRegisterClick(view: View) {
        val email = findViewById<EditText>(R.id.email_register).text.toString()
        val password = findViewById<EditText>(R.id.password_register).text.toString()

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "Registered successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to register", Toast.LENGTH_SHORT).show()
            }
        }
    }


}