package com.example.dormapp

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.dormapp.api.GivePointRequest
import com.example.dormapp.api.GivePointResponse
import com.example.dormapp.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GivePointFragment : Fragment(R.layout.fragment_give_point) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etStudentId = view.findViewById<EditText>(R.id.et_student_id)
        val etPoint = view.findViewById<EditText>(R.id.et_point)
        val rgType = view.findViewById<RadioGroup>(R.id.rg_point_type)
        val btnGive = view.findViewById<Button>(R.id.btn_give_point)

        btnGive.setOnClickListener {
            val studentId = etStudentId.text.toString().trim()
            val pointStr = etPoint.text.toString().trim()
            val checkedRadioId = rgType.checkedRadioButtonId

            if (studentId.isEmpty() || pointStr.isEmpty() || checkedRadioId == -1) {
                Toast.makeText(requireContext(), "모든 값을 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val type = when (checkedRadioId) {
                R.id.rb_reward -> "reward"
                R.id.rb_penalty -> "penalty"
                else -> ""
            }
            val point = pointStr.toIntOrNull() ?: 0
            if (point == 0) {
                Toast.makeText(requireContext(), "점수를 올바르게 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
            val token = prefs.getString("auth_token", null)
            if (token.isNullOrBlank()) {
                Toast.makeText(requireContext(), "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val req = GivePointRequest(
                studentId = studentId,
                pointType = type,
                point = point
            )
            val api = RetrofitClient.create(requireContext())
            api.givePoint(req).enqueue(object : Callback<GivePointResponse> {
                override fun onResponse(
                    call: Call<GivePointResponse>,
                    response: Response<GivePointResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(requireContext(), "점수 부여 성공!", Toast.LENGTH_SHORT).show()
                        etStudentId.text.clear()
                        etPoint.text.clear()
                        rgType.clearCheck()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            response.body()?.error ?: "실패",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<GivePointResponse>, t: Throwable) {
                    Toast.makeText(
                        requireContext(),
                        "네트워크 오류: ${t.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }
}
