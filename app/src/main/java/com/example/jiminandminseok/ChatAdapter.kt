package com.example.jiminandminseok

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.VH>() {

    private val items = mutableListOf<Message>()

    fun submitList(newItems: List<Message>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun add(message: Message) {
        items.add(message)
        notifyItemInserted(items.lastIndex)
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: MaterialCardView = itemView.findViewById(R.id.cardBubble)
        val tv: TextView = itemView.findViewById(R.id.tvBubble)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.tv.text = if (item.isUser) "나: ${item.text}" else "상담봇: ${item.text}"

        val lp = holder.card.layoutParams as FrameLayout.LayoutParams
        lp.gravity = if (item.isUser) Gravity.END else Gravity.START
        holder.card.layoutParams = lp
    }

    override fun getItemCount(): Int = items.size
}
