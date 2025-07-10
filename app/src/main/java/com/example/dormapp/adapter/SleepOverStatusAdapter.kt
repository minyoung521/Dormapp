package com.example.dormapp.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dormapp.R
import com.example.dormapp.api.SleepOverStatus

class SleepOverStatusAdapter(
    private val items: List<SleepOverStatus>,
    private val isAdmin: Boolean,
    private val onApprove: (Int) -> Unit,
    private val onReject: (Int) -> Unit
) : RecyclerView.Adapter<SleepOverStatusAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvStudentNumber: TextView = itemView.findViewById(R.id.tvStudentNumber)
        val tvName: TextView          = itemView.findViewById(R.id.tvName)
        val tvDate: TextView          = itemView.findViewById(R.id.tvDate)
        val tvStatus: TextView        = itemView.findViewById(R.id.tvStatus)
        val btnApprove: Button        = itemView.findViewById(R.id.btnApprove)
        val btnReject: Button         = itemView.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sleepover_status, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvStudentNumber.text = item.student_number
        holder.tvName.text          = item.name
        holder.tvDate.text          = item.out_date

        when (item.status) {
            "approved" -> {
                holder.tvStatus.text = "승인"
                holder.tvStatus.setTextColor(Color.parseColor("#388E3C"))
            }
            "rejected" -> {
                holder.tvStatus.text = "거절"
                holder.tvStatus.setTextColor(Color.parseColor("#D32F2F"))
            }
            else -> {
                holder.tvStatus.text = "미승인"
                holder.tvStatus.setTextColor(Color.parseColor("#F57C00"))
            }
        }

        if (isAdmin) {
            holder.btnApprove.visibility = View.VISIBLE
            holder.btnReject.visibility  = View.VISIBLE
            holder.btnApprove.setOnClickListener { onApprove(item.id) }
            holder.btnReject.setOnClickListener  { onReject(item.id) }
        } else {
            holder.btnApprove.visibility = View.GONE
            holder.btnReject.visibility  = View.GONE
        }
    }
}
