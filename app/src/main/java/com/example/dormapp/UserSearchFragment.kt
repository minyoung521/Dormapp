package com.example.dormapp

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dormapp.adapter.UserAdminAdapter
import com.example.dormapp.api.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserSearchFragment : Fragment() {

    private lateinit var etSearchInput: EditText
    private lateinit var btnSearchUser: Button
    private lateinit var rvUserSearchResult: RecyclerView
    private lateinit var tvEmptyResult: TextView
    private lateinit var adapter: UserAdminAdapter
    private lateinit var apiService: ApiService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_search, container, false)
        etSearchInput = view.findViewById(R.id.etSearchInput)
        btnSearchUser = view.findViewById(R.id.btnSearchUser)
        rvUserSearchResult = view.findViewById(R.id.rvUserSearchResult)
        tvEmptyResult = view.findViewById(R.id.tvEmptyResult)
        apiService = RetrofitClient.create(requireContext())

        adapter = UserAdminAdapter(emptyList()) { user ->
            fetchUserDetailAndShowDialog(user.id)
        }
        rvUserSearchResult.layoutManager = LinearLayoutManager(requireContext())
        rvUserSearchResult.adapter = adapter
        tvEmptyResult.visibility = View.GONE

        btnSearchUser.setOnClickListener {
            val keyword = etSearchInput.text.toString().trim()
            if (keyword.isEmpty()) {
                Toast.makeText(requireContext(), "학번을 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            searchUser(keyword)
        }

        return view
    }

    private fun searchUser(studentNumber: String) {
        apiService.searchUser(studentNumber).enqueue(object : Callback<UserSearchListResponse> {
            override fun onResponse(
                call: Call<UserSearchListResponse>,
                response: Response<UserSearchListResponse>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val users = response.body()?.users ?: emptyList()
                    Log.d("UserSearch", "Users found: ${users.size}")
                    users.forEach { user ->
                        Log.d("UserSearch", "User - fullName: ${user.fullName}, studentNumber: ${user.studentNumber}, department: ${user.department}")
                    }
                    adapter.updateData(users)
                    tvEmptyResult.visibility = if (users.isEmpty()) View.VISIBLE else View.GONE
                } else {
                    adapter.updateData(emptyList())
                    tvEmptyResult.visibility = View.VISIBLE
                }
            }
            override fun onFailure(call: Call<UserSearchListResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "검색 실패: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchUserDetailAndShowDialog(userId: Int) {
        apiService.getAdminUserDetail(userId).enqueue(object : Callback<AdminUserDetailResponse> {
            override fun onResponse(
                call: Call<AdminUserDetailResponse>,
                response: Response<AdminUserDetailResponse>
            ) {
                val user = response.body()?.user
                if (user != null) showUserDetailDialog(user)
                else Toast.makeText(requireContext(), "회원 정보 없음", Toast.LENGTH_SHORT).show()
            }
            override fun onFailure(call: Call<AdminUserDetailResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "상세정보 불러오기 실패: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showUserDetailDialog(user: AdminUserDetail) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_user_detail, null)

        val etName = dialogView.findViewById<EditText>(R.id.etDetailName)
        val etStudentNumber = dialogView.findViewById<EditText>(R.id.etDetailStudentNumber)
        val etDepartment = dialogView.findViewById<EditText>(R.id.etDetailDepartment)
        val etPhone = dialogView.findViewById<EditText>(R.id.etDetailPhone)
        val etReward = dialogView.findViewById<EditText>(R.id.etDetailReward)
        val etPenalty = dialogView.findViewById<EditText>(R.id.etDetailPenalty)
        val tvDorm = dialogView.findViewById<TextView>(R.id.tvDetailDorm)

        etName.setText(user.fullName ?: "")
        etStudentNumber.setText(user.studentNumber ?: "")
        etDepartment.setText(user.department ?: "")
        etPhone.setText(user.phoneNumber ?: "")
        etReward.setText(user.rewardPoint.toString())
        etPenalty.setText(user.penaltyPoint.toString())
        tvDorm.text = "관/호실: ${user.buildingName ?: ""} ${user.rNumber}호"

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("수정", null)
            .setNegativeButton("삭제", null)
            .setNeutralButton("닫기", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val newName = etName.text.toString()
                val newStudentNumber = etStudentNumber.text.toString()
                val newDepartment = etDepartment.text.toString()
                val newPhone = etPhone.text.toString()
                val newReward = etReward.text.toString().toIntOrNull() ?: 0
                val newPenalty = etPenalty.text.toString().toIntOrNull() ?: 0

                val params = mutableMapOf<String, Any>(
                    "full_name" to newName,
                    "student_number" to newStudentNumber,
                    "department" to newDepartment,
                    "phone_number" to newPhone,
                    "reward_point" to newReward,
                    "penalty_point" to newPenalty
                )
                apiService.updateAdminUser(user.id, params).enqueue(object : Callback<AdminUserDetailResponse> {
                    override fun onResponse(
                        call: Call<AdminUserDetailResponse>,
                        response: Response<AdminUserDetailResponse>
                    ) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            Toast.makeText(requireContext(), "수정 완료", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                            searchUser(newStudentNumber)
                        } else {
                            Toast.makeText(requireContext(), "수정 실패", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<AdminUserDetailResponse>, t: Throwable) {
                        Toast.makeText(requireContext(), "수정 실패: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
                apiService.deleteAdminUser(user.id).enqueue(object : Callback<DeleteResponse> {
                    override fun onResponse(call: Call<DeleteResponse>, response: Response<DeleteResponse>) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            Toast.makeText(requireContext(), "삭제 완료", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                            adapter.updateData(emptyList())
                        } else {
                            Toast.makeText(requireContext(), "삭제 실패", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<DeleteResponse>, t: Throwable) {
                        Toast.makeText(requireContext(), "삭제 실패: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                dialog.dismiss()
            }
        }
        dialog.show()
    }
}
