package com.dudko.shoppinglist

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

data class ShoppingItem(var name: String, var price: Float, var quantity: Long, var checked: Boolean = false)

