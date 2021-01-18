package com.dudko.shoppinglist

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast

class LoginActivity() : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)
    }

    fun onLoginClick(view: View) {
        val email = findViewById<EditText>(R.id.email_login).text.toString()
        val password = findViewById<EditText>(R.id.password_login).text.toString()

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "Signed in successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to login", Toast.LENGTH_SHORT).show()
            }
        }
    }
}