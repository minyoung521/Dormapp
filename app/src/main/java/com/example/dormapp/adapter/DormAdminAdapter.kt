package com.example.dormapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dormapp.api.DormApplyListItem

class DormAdminAdapter(
    private var items: List<DormApplyListItem>,
    private val onItemClick: (DormApplyListItem) -> Unit
) : RecyclerView.Adapter<DormAdminAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvStudentNum: TextView = view.findViewById(R.id.tvStudentNum)
        val tvGender: TextView = view.findViewById(R.id.tvGender)
        val tvContent: TextView = view.findViewById(R.id.tvContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dorm_apply, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = "이름: ${item.name}"
        holder.tvStudentNum.text = "학번: ${item.student_number}"
        holder.tvGender.text = "성별: ${item.gender}"
        holder.tvContent.text = "문의: ${item.content}"

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size

    // 리스트 갱신이 필요할 경우 함수 추가 (옵션)
    fun updateList(newItems: List<DormApplyListItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
