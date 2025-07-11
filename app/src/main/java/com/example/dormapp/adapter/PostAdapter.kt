package com.example.dormapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dormapp.R
import com.example.dormapp.api.PostData
import com.example.dormapp.databinding.ItemPostBinding

class PostAdapter(
    private val items: MutableList<PostData>,
    private val onClick: (PostData) -> Unit,
    private val onLikeClick: (PostData, Int) -> Unit
) : RecyclerView.Adapter<PostAdapter.VH>() {

    inner class VH(private val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PostData) {
            binding.tvAuthor.text = "익명${item.anonAuthor ?: "0000"}"
            binding.tvTitle.text = item.title
            binding.tvCreated.text = item.createdAt.substring(0, 10)
            binding.tvLikeCount.text = item.likeCount.toString()
            binding.ivLikeIcon.setImageResource(
                if (item.isLiked) R.drawable.ic_heart_filled
                else R.drawable.ic_heart_outline
            )

            val commentCount = item.comments?.size ?: 0
            binding.tvCommentCount.text = "댓글 ${commentCount}개"

            binding.ivLikeIcon.setOnClickListener {
                onLikeClick(item, adapterPosition)
            }
            binding.root.setOnClickListener {
                onClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemPostBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        Log.d("PostAdapter", "bind: $position, title=${items[position].title}")
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newList: List<PostData>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}
