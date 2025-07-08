package com.example.dormapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.dormapp.api.InquiryData
import com.example.dormapp.R

class InquiryAdapter(
    private val onItemClick: (inquiryId: Int) -> Unit
) : RecyclerView.Adapter<InquiryAdapter.ViewHolder>() {
    private var items = listOf<InquiryData>()

    fun updateList(list: List<InquiryData>) {
        items = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_inquiry_item_title)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_inquiry_item_date)
        private val tvAnswer: TextView = itemView.findViewById(R.id.tv_inquiry_item_answer)

        fun bind(item: InquiryData) {
            tvTitle.text = item.title
            tvDate.text = item.createdAt.substring(0, 10)
            if (item.answer != null) {
                tvAnswer.visibility = View.VISIBLE
                tvAnswer.text = "답변완료"
                tvAnswer.setTextColor(itemView.context.getColor(R.color.green_500))
            } else {
                tvAnswer.visibility = View.VISIBLE
                tvAnswer.text = "미답변"
                tvAnswer.setTextColor(itemView.context.getColor(R.color.red_500))
            }
            itemView.setOnClickListener {
                onItemClick(item.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inquiry, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
}