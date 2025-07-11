package com.example.dormapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dormapp.api.NoticeData
import com.example.dormapp.api.RetrofitClient
import com.example.dormapp.databinding.ItemNoticeBinding

class NoticeAdapter(
    private val items: MutableList<NoticeData>,
    private val onClick: (NoticeData) -> Unit
) : RecyclerView.Adapter<NoticeAdapter.VH>() {

    inner class VH(private val binding: ItemNoticeBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NoticeData) {
            binding.tvNoticeTitle.text = item.title
            binding.tvNoticeDate.text  = item.date
            if (!item.imageUrl.isNullOrEmpty()) {
                binding.ivNoticeThumb.visibility = View.VISIBLE
                Glide.with(binding.ivNoticeThumb.context)
                    .load(RetrofitClient.BASE_URL + item.imageUrl)
                    .into(binding.ivNoticeThumb)
            } else {
                binding.ivNoticeThumb.visibility = View.GONE
            }
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemNoticeBinding.inflate(
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
