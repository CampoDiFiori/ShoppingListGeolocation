package com.dudko.shoppinglist

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

class ShoppingListAdapter(private val context: Context, val ref: DatabaseReference)
    : RecyclerView.Adapter<ShoppingListAdapter.ShoppingItemHolder>()
{

    private var items: HashMap<String, ShoppingItem> = hashMapOf()

    private fun snapshotToItem(snapshot: DataSnapshot): Pair<String?, ShoppingItem>{
        val newItemHashMap = snapshot.value as Map<String, Object>

        val quantity = newItemHashMap["quantity"] as Long
        val name = newItemHashMap["name"] as String
        val price = newItemHashMap["price"].toString().toFloat()
        val checked = newItemHashMap["checked"] as Boolean
        return Pair(snapshot.key, ShoppingItem(name, price, quantity, checked))
    }


    init {
        ref.addChildEventListener(object: ChildEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.e("ListAdapter", "Event Cancelled")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Log.e("ListAdapter", "Child moved")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                CoroutineScope(IO).launch {
                    val (itemId, changedItem) = snapshotToItem(snapshot)
                    if (itemId == null) {
                        return@launch
                    }

                    items[itemId] = changedItem

                    withContext(Main) {
                        notifyDataSetChanged()
                    }
                }
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                CoroutineScope(IO).launch {
                    val (itemId, newItem) = snapshotToItem(snapshot)
                    if (itemId == null) {
                        return@launch
                    }

                    items[itemId] = newItem

                    withContext(Main) {
                        notifyDataSetChanged()
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                CoroutineScope(IO).launch {
                    val itemId = snapshot.key ?: return@launch
                    items.remove(itemId)

                    withContext(Main) {
                        notifyDataSetChanged()
                    }
                }
            }

        })
    }

    inner class ShoppingItemHolder(val view: View)
        : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingItemHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.shopping_item, parent, false)
        return ShoppingItemHolder(view)
    }

    override fun onBindViewHolder(holder: ShoppingItemHolder, position: Int) {
        val sharedPreferences = context.getSharedPreferences("shopping_options", Context.MODE_PRIVATE)
        val currencyPln = sharedPreferences.getBoolean("currency_pln", true)


        val (currentItemId, currentItem) = items.toList()[position]
        val title = holder.view.findViewById<TextView>(R.id.itemName)
        val price = holder.view.findViewById<TextView>(R.id.itemPrice)
        val checked = holder.view.findViewById<CheckBox>(R.id.checked)
        val deleteButton = holder.view.findViewById<Button>(R.id.deleteProduct)

        title.text = "${currentItem.name} (${currentItem.quantity})"
        price.text = "${currentItem.price.toString()} ${if (currencyPln) {"PLN"} else {"EUR"}}"
        checked.isChecked = currentItem.checked

        holder.view.setOnClickListener {

            val editProductIntent = Intent(context, EditProductActivity::class.java).apply {
                putExtra("id", currentItemId)
                putExtra("name", currentItem.name)
                putExtra("price", currentItem.price)
                putExtra("quantity", currentItem.quantity)
                putExtra("checked", currentItem.checked)
            }
            startActivity(context, editProductIntent, null)
        }

        checked.setOnClickListener {
            currentItem.checked = !currentItem.checked
            CoroutineScope(Dispatchers.IO).launch {
                ref.child(currentItemId).setValue(currentItem)
            }
        }

        deleteButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                ref.child(currentItemId).removeValue()
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}