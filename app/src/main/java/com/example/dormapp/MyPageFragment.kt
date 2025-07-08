package com.example.dormapp

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.dormapp.api.MyPageResponse
import com.example.dormapp.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyPageFragment : Fragment(R.layout.fragment_mypage) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvName         = view.findViewById<TextView>(R.id.tvName)
        val tvStudentNum   = view.findViewById<TextView>(R.id.tvStudentNumber)
        val tvDepartment   = view.findViewById<TextView>(R.id.tvDepartment)
        val tvRewardPoint  = view.findViewById<TextView>(R.id.tvRewardPoint)
        val tvPenaltyPoint = view.findViewById<TextView>(R.id.tvPenaltyPoint)
        val tvDorm         = view.findViewById<TextView>(R.id.tvDorm)
        val btnScoreAdmin  = view.findViewById<Button>(R.id.btnScoreAdmin)
        val btnInquiry     = view.findViewById<Button>(R.id.btnInquiry)

        btnScoreAdmin.visibility = View.GONE

        val prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val token = prefs.getString("auth_token", null)
        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.create(requireContext())
            .getMyPage()
            .enqueue(object : Callback<MyPageResponse> {
                override fun onResponse(call: Call<MyPageResponse>, response: Response<MyPageResponse>) {
                    val data = response.body()?.mypage
                    if (response.isSuccessful && data != null) {
                        tvName.text         = "이름: ${data.user.fullName ?: data.user.username}"
                        tvStudentNum.text   = "학번: ${data.user.username}"
                        tvDepartment.text   = "학과: ${data.user.department}"
                        tvRewardPoint.text  = "상점: ${data.user.rewardPoint}"
                        tvPenaltyPoint.text = "벌점: ${data.user.penaltyPoint}"
                        val dormInfo = data.dorm
                            ?.let { "${it.building_name} ${it.r_number}호 (좌석: ${it.position})" }
                            ?: "미신청"
                        tvDorm.text = "관/호실: $dormInfo"

                        if (data.user.isStaff || data.user.isSuperuser) {
                            btnScoreAdmin.visibility = View.VISIBLE
                        }
                    } else {
                        Toast.makeText(requireContext(), "정보 불러오기 실패", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<MyPageResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "네트워크 오류: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            })

        btnInquiry.setOnClickListener {
            findNavController().navigate(
                R.id.action_myPageFragment_to_inquiryListFragment
            )
        }
        btnScoreAdmin.setOnClickListener {
            findNavController().navigate(
                R.id.action_myPageFragment_to_givePointFragment
            )
        }
    }
}
