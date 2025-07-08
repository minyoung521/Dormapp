package com.example.dormapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dormapp.api.NoticeData
import com.example.dormapp.databinding.ItemNoticeTextBinding

class NoticeAdapter(
    private val items: MutableList<NoticeData>,
    private val onClick: (NoticeData) -> Unit
) : RecyclerView.Adapter<NoticeAdapter.VH>() {

    inner class VH(private val binding: ItemNoticeTextBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NoticeData) {
            binding.tvNoticeTitle.text = item.title
            binding.tvNoticeDate.text  = item.date
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemNoticeTextBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newList: List<NoticeData>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}
