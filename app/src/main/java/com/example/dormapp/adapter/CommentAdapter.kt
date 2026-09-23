package com.example.dormapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dormapp.api.CommentData
import com.example.dormapp.databinding.ItemCommentBinding

class CommentAdapter(
    private val myUserId: Int,
    private val isAdmin: Boolean,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

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

            val isMyComment = comment.authorId == myUserId
            if (isMyComment || isAdmin) {
                binding.btnDeleteComment.visibility = View.VISIBLE
                binding.btnDeleteComment.setOnClickListener {
                    onDeleteClick(comment.id)
                }
            } else {
                binding.btnDeleteComment.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }
}
