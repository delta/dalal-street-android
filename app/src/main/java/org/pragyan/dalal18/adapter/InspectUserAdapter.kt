package org.pragyan.dalal18.adapter


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dalalstreet.api.models.InspectDetailsOuterClass
import kotlinx.android.synthetic.main.inspect_user_list_item.view.*
import org.pragyan.dalal18.R

 class InspectUserAdapter(val items :  MutableList<InspectDetailsOuterClass.InspectDetails>, val context: Context) : RecyclerView.Adapter<ViewHolder>() {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.inspect_user_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.inspectUserUserId?.text = "User Id :" + items.get(position).id.toString()
        holder.inspectUserEmail?.text ="Email :" + items.get(position).email
        holder.TransactionCount?.text = "Transaction Count :" +items.get(position).transactionCount.toString()
        holder.inspectUserPosition?.text = "Position :" +items.get(position).position.toString()
        holder.StockSum?.text = "StockSum :" +items.get(position).stockSum.toString()
    }
}

class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val inspectUserUserId = view.userIdTextview
    val inspectUserEmail = view.email_textView
    val TransactionCount = view.transactionCount_textView
    val inspectUserPosition = view.position_textView
    val StockSum = view.stocksum_textview
}