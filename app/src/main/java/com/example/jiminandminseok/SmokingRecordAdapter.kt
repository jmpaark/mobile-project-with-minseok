package com.example.jiminandminseok

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SmokingRecordAdapter : RecyclerView.Adapter<SmokingRecordAdapter.RecordViewHolder>() {
    private val items = mutableListOf<SmokingRecord>()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    fun submitList(records: List<SmokingRecord>) {
        items.clear()
        items.addAll(records)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_record, parent, false)
        return RecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        holder.bind(items[position], dateFormat)
    }

    override fun getItemCount(): Int = items.size

    class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCount = itemView.findViewById<TextView>(R.id.tvCount)
        private val tvMemo = itemView.findViewById<TextView>(R.id.tvMemo)
        private val tvDate = itemView.findViewById<TextView>(R.id.tvDate)

        fun bind(record: SmokingRecord, format: SimpleDateFormat) {
            tvCount.text = itemView.context.getString(R.string.record_count_format, record.count)
            tvMemo.text = record.memo ?: ""
            tvDate.text = format.format(Date(record.createdAt))
        }
    }
}
