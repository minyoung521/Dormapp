package com.example.dormapp

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.dormapp.api.InquiryAnswerRequest
import com.example.dormapp.api.InquiryAnswerResponse
import com.example.dormapp.api.InquiryDetailResponse
import com.example.dormapp.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InquiryDetailFragment : Fragment(R.layout.fragment_inquiry_detail) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvTitle = view.findViewById<TextView>(R.id.tv_inquiry_detail_title)
        val tvContent = view.findViewById<TextView>(R.id.tv_inquiry_detail_content)
        val tvDate = view.findViewById<TextView>(R.id.tv_inquiry_detail_date)
        val tvAnswerLabel = view.findViewById<TextView>(R.id.tv_inquiry_answer_label)
        val tvAnswerContent = view.findViewById<TextView>(R.id.tv_inquiry_answer_content)
        val etAnswer = view.findViewById<EditText>(R.id.et_inquiry_answer)
        val btnSubmitAnswer = view.findViewById<Button>(R.id.btn_submit_answer)

        val inquiryId = arguments?.getInt("inquiryId") ?: return

        val prefs = requireContext()
            .getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val token = prefs.getString("auth_token", null)
        val isStaff = prefs.getBoolean("is_staff", false)

        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val api = RetrofitClient.create(requireContext())
        api.getInquiryDetail(inquiryId).enqueue(object : Callback<InquiryDetailResponse> {
            override fun onResponse(
                call: Call<InquiryDetailResponse>,
                response: Response<InquiryDetailResponse>
            ) {
                val data = response.body()?.inquiry
                if (response.isSuccessful && data != null) {
                    tvTitle.text = data.title
                    tvContent.text = data.content
                    tvDate.text = data.createdAt

                    if (data.answer != null) {
                        tvAnswerLabel.visibility = View.VISIBLE
                        tvAnswerContent.visibility = View.VISIBLE
                        tvAnswerContent.text = data.answer.answer
                        etAnswer.visibility = View.GONE
                        btnSubmitAnswer.visibility = View.GONE
                    } else {
                        if (isStaff) {
                            etAnswer.visibility = View.VISIBLE
                            btnSubmitAnswer.visibility = View.VISIBLE
                            tvAnswerLabel.visibility = View.GONE
                            tvAnswerContent.visibility = View.GONE

                            btnSubmitAnswer.setOnClickListener {
                                val answerText = etAnswer.text.toString().trim()
                                if (answerText.isEmpty()) {
                                    Toast.makeText(requireContext(), "답변을 입력하세요", Toast.LENGTH_SHORT).show()
                                    return@setOnClickListener
                                }
                                api.answerInquiry(inquiryId, InquiryAnswerRequest(answerText))
                                    .enqueue(object : Callback<InquiryAnswerResponse> {
                                        override fun onResponse(
                                            call: Call<InquiryAnswerResponse>,
                                            response: Response<InquiryAnswerResponse>
                                        ) {
                                            if (response.isSuccessful && response.body()?.success == true) {
                                                Toast.makeText(requireContext(), "답변 등록 완료!", Toast.LENGTH_SHORT).show()
                                                requireActivity().onBackPressedDispatcher.onBackPressed()
                                            } else {
                                                Toast.makeText(requireContext(), "답변 등록 실패", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        override fun onFailure(call: Call<InquiryAnswerResponse>, t: Throwable) {
                                            Toast.makeText(
                                                requireContext(),
                                                "네트워크 오류: ${t.localizedMessage}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    })
                            }
                        } else {
                            tvAnswerLabel.visibility = View.GONE
                            tvAnswerContent.visibility = View.GONE
                            etAnswer.visibility = View.GONE
                            btnSubmitAnswer.visibility = View.GONE
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "문의 상세 조회 실패", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<InquiryDetailResponse>, t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "네트워크 오류: ${t.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
