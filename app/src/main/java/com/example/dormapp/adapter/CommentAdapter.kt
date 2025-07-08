package com.example.dormapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dormapp.api.CommentData
import com.example.dormapp.databinding.ItemCommentBinding

class CommentAdapter : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {
    private var items = listOf<CommentData>()

    fun updateList(list: List<CommentData>) {
        items = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: CommentData) {
            binding.tvCommentAuthor.text = comment.anonAuthor ?: "익명"
            binding.tvCommentContent.text = comment.content
            binding.tvCommentDate.text = comment.createdAt
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }
}
