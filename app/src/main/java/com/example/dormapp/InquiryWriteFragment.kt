package com.example.dormapp

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.dormapp.api.InquiryCreateRequest
import com.example.dormapp.api.InquiryCreateResponse
import com.example.dormapp.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InquiryWriteFragment : Fragment(R.layout.fragment_inquiry_write) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etTitle = view.findViewById<EditText>(R.id.et_inquiry_title)
        val etContent = view.findViewById<EditText>(R.id.et_inquiry_content)
        val btnSubmit = view.findViewById<Button>(R.id.btn_inquiry_submit)

        btnSubmit.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val content = etContent.text.toString().trim()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(requireContext(), "제목과 내용을 모두 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
            val token = prefs.getString("auth_token", null)
            if (token.isNullOrBlank()) {
                Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val api = RetrofitClient.create(requireContext())
            api.createInquiry(InquiryCreateRequest(title, content))
                .enqueue(object : Callback<InquiryCreateResponse> {
                    override fun onResponse(
                        call: Call<InquiryCreateResponse>,
                        response: Response<InquiryCreateResponse>
                    ) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            Toast.makeText(requireContext(), "문의가 등록되었습니다.", Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                response.body()?.error ?: "등록 실패",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    override fun onFailure(call: Call<InquiryCreateResponse>, t: Throwable) {
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
