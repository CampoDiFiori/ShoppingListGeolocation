package com.dudko.shoppinglist

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditProductActivity: BaseActivity() {

    private var editedProductId: String? = null // if it's null, we're adding a new product
    private var productChecked: Boolean = false // here we're not editing it but we'd like to store it temporarily for convenience

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_product)

        editedProductId = intent.getStringExtra("id")

        val nameContent = findViewById<EditText>(R.id.itemName)
        val priceContent = findViewById<EditText>(R.id.itemPrice)
        val quantityContent = findViewById<EditText>(R.id.itemQuantity)

        if (editedProductId == null) {
            this.title = "Add a product"
        } else {
            this.title = "Edit product"

            val passedName = intent.getStringExtra("name")
            val passedPrice = intent.getFloatExtra("price", 0.0f)
            val passedQuantity = intent.getLongExtra("quantity", 0)
            productChecked = intent.getBooleanExtra("checked", false)

            nameContent.setText(passedName)
            priceContent.setText(passedPrice.toString())
            quantityContent.setText(passedQuantity.toString())
        }
    }

    fun onOKClick(view: View) {
        val name = findViewById<EditText>(R.id.itemName).text.toString()
        val priceString = findViewById<EditText>(R.id.itemPrice).text.toString()
        val quantityString = findViewById<EditText>(R.id.itemQuantity).text.toString()

        if (name.isEmpty() || priceString.isEmpty() || quantityString.isEmpty()) {
            Toast.makeText(this, "Fill out all the inputs first", Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceString.toFloat()
        val quantity = quantityString.toLong()

        val database = FirebaseDatabase.getInstance()
        val uid = mAuth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "You are not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val ref = database.getReference(uid)

        CoroutineScope(Dispatchers.IO).launch {
            val newItem = ShoppingItem(
                name = name,
                price = price,
                quantity = quantity,
                checked = productChecked
            )
            if (editedProductId == null) {
                ref.push().setValue(newItem)
            } else {
                ref.child(editedProductId!!).setValue(newItem)
            }
        }
        finish()
    }

    fun onCancelClick(view: View) {
        finish()
    }
}