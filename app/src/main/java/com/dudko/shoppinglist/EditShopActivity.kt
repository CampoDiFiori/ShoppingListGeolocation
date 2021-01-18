package com.dudko.shoppinglist

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditShopActivity: BaseActivity() {

    private var editedShopId: String? = null // if it's null, we're adding a new product
    private var passedLon: Double = 0.0
    private var passedLat: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_shop)

        editedShopId = intent.getStringExtra("id")

        val nameContent = findViewById<EditText>(R.id.shopName)
        val descriptionContent = findViewById<EditText>(R.id.shopDescription)
        val radiusContent = findViewById<EditText>(R.id.shopRadius)

        this.title = "Edit shop"

        val passedName = intent.getStringExtra("name")
        val passedDesc = intent.getStringExtra("description")
        val passedRadius = intent.getFloatExtra("radius", 0f)

        passedLon = intent.getDoubleExtra("longitude", 0.0)
        passedLat = intent.getDoubleExtra("latitude", 0.0)

        nameContent.setText(passedName)
        descriptionContent.setText(passedDesc)
        radiusContent.setText(passedRadius.toString())
    }

    fun onOKClick(view: View) {
        val name = findViewById<EditText>(R.id.shopName).text.toString()
        val desc = findViewById<EditText>(R.id.shopDescription).text.toString()
        val radius = findViewById<EditText>(R.id.shopRadius).text.toString()

        if (name.isEmpty() || desc.isEmpty() || radius.isEmpty()) {
            Toast.makeText(this, "Fill out all the inputs first", Toast.LENGTH_SHORT).show()
            return
        }

        val database = FirebaseDatabase.getInstance()
        val uid = mAuth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "You are not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val ref = database.getReference("Favorite Shops by $uid")

        CoroutineScope(Dispatchers.IO).launch {
            val newItem = Shop(
                name = name,
                description = desc,
                radius = radius.toFloat(),
                inside = false,
                longitude = passedLon,
                latitude = passedLat
            )
            ref.child(editedShopId!!).setValue(newItem)
        }
        finish()
    }

    fun onCancelClick(view: View) {
        finish()
    }
}
