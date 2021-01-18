package com.dudko.shoppinglist

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

/**
 * A helper class used for setting a theme of the app in every Activity
 */
open class BaseActivity : AppCompatActivity() {

    private var darkTheme: Boolean = false
    protected lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        darkTheme = getDarkThemeFromSharedPreferences()
        when (darkTheme) {
            true -> {
                setTheme(R.style.Theme_AppCompat)
            }
            false -> {
                setTheme(R.style.ThemeOverlay_AppCompat_Light)
            }
        }
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
    }

    override fun onResume() {
        super.onResume()
        if (darkTheme != getDarkThemeFromSharedPreferences()) {
            this.recreate()
        }
    }

    private fun getDarkThemeFromSharedPreferences(): Boolean {
        val sharedPreferences = getSharedPreferences("shopping_options", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("dark_theme", false)
    }
}