package com.example.dormapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dormapp.R
import com.example.dormapp.api.UserSearchData

class UserAdminAdapter(
    private var userList: List<UserSearchData> = emptyList(),
    private val onItemClick: (UserSearchData) -> Unit
) : RecyclerView.Adapter<UserAdminAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvUserStudentNumber: TextView = itemView.findViewById(R.id.tvUserStudentNumber)
        val tvUserDepartment: TextView = itemView.findViewById(R.id.tvUserDepartment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_search, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        Log.d("UserAdminAdapter", "Binding user: ${user.fullName}, ${user.studentNumber}, ${user.department}")
        holder.tvUserName.text = "이름: ${user.fullName ?: "정보없음"}"
        holder.tvUserStudentNumber.text = "학번: ${user.studentNumber ?: "정보없음"}"
        holder.tvUserDepartment.text = "학과: ${user.department ?: "정보없음"}"

        holder.itemView.setOnClickListener { onItemClick(user) }
    }

    override fun getItemCount(): Int = userList.size

    fun updateData(newList: List<UserSearchData>) {
        userList = newList
        notifyDataSetChanged()
    }
}
