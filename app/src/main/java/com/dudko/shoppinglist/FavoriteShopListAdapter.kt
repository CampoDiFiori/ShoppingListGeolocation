package com.dudko.shoppinglist

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode

data class Shop(val name: String, val description: String, val radius: Float, val inside: Boolean, val longitude: Double, val latitude: Double)

class FavoriteShopListAdapter(private val context: Context, val ref: DatabaseReference)
    : RecyclerView.Adapter<FavoriteShopListAdapter.FavoriteShopItemHolder>()
{

    private var shops: HashMap<String, Shop> = hashMapOf()

    private fun snapshotToShop(snapshot: DataSnapshot): Pair<String?, Shop>{
        val newShopHashMap = snapshot.value as Map<String, Object>

        val name = newShopHashMap["name"] as String
        val description = newShopHashMap["description"] as String
        val radius = newShopHashMap["radius"].toString().toFloat()
        val inside = newShopHashMap["inside"] as Boolean
        val longitude = newShopHashMap["longitude"].toString().toDouble()
        val latitude = newShopHashMap["latitude"].toString().toDouble()
        return Pair(snapshot.key, Shop(name, description, radius, inside, longitude, latitude))
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
                CoroutineScope(Dispatchers.IO).launch {
                    val (shopId, changedShop) = snapshotToShop(snapshot)
                    if (shopId == null) {
                        return@launch
                    }

                    shops[shopId] = changedShop

                    withContext(Dispatchers.Main) {
                        notifyDataSetChanged()
                    }
                }
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                CoroutineScope(Dispatchers.IO).launch {
                    val (shopId, newShop) = snapshotToShop(snapshot)
                    if (shopId == null) {
                        return@launch
                    }

                    shops[shopId] = newShop

                    withContext(Dispatchers.Main) {
                        notifyDataSetChanged()
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                CoroutineScope(Dispatchers.IO).launch {
                    val shopId = snapshot.key ?: return@launch
                    shops.remove(shopId)

                    withContext(Dispatchers.Main) {
                        notifyDataSetChanged()
                    }
                }
            }

        })
    }

    inner class FavoriteShopItemHolder(val view: View)
        : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteShopItemHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.favorite_shop, parent, false)
        return FavoriteShopItemHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteShopItemHolder, position: Int) {

        val (currentShopId, currentShop) = shops.toList()[position]
        val name = holder.view.findViewById<TextView>(R.id.shopName)
        val description = holder.view.findViewById<TextView>(R.id.shopDescription)
        val radius = holder.view.findViewById<TextView>(R.id.shopRadius)
        val deleteShopButton = holder.view.findViewById<Button>(R.id.deleteShop)

        name.text = "${currentShop.name} (Lon: ${BigDecimal(currentShop.longitude).setScale(2, RoundingMode.HALF_EVEN)}, Lat: ${BigDecimal(currentShop.latitude).setScale(2, RoundingMode.HALF_EVEN)})"
        description.text = currentShop.description
        radius.text = currentShop.radius.toString()

        holder.view.setOnClickListener {
            val editShopIntent = Intent(context, EditShopActivity::class.java).apply {
                putExtra("id", currentShopId)
                putExtra("name", currentShop.name)
                putExtra("description", currentShop.description)
                putExtra("radius", currentShop.radius)
                putExtra("latitude", currentShop.latitude)
                putExtra("longitude", currentShop.longitude)
            }
            ContextCompat.startActivity(context, editShopIntent, null)
        }

        deleteShopButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                ref.child(currentShopId).removeValue()
            }
        }
    }

    override fun getItemCount(): Int {
        return shops.size
    }
}
