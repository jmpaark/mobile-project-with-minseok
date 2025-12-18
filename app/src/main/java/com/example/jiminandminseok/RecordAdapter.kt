package com.example.jiminandminseok

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordAdapter(
    private val onItemClick: (RecordEntity) -> Unit = {}
) : RecyclerView.Adapter<RecordAdapter.VH>() {

    private val items = mutableListOf<RecordEntity>()
    private val df = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    fun submitList(newItems: List<RecordEntity>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCount: TextView = itemView.findViewById(R.id.tvCount)
        val tvMemo: TextView = itemView.findViewById(R.id.tvMemo)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)

        init {
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onItemClick(items[pos])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_record, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.tvCount.text = "오늘 ${item.count}개"
        holder.tvMemo.text = item.memo?.takeIf { it.isNotBlank() } ?: "메모 없음"
        holder.tvDate.text = df.format(Date(item.createdAt))
    }

    override fun getItemCount(): Int = items.size
}
