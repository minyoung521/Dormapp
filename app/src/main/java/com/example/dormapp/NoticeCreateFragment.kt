package com.example.dormapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.dormapp.api.NoticeCreateRequest
import com.example.dormapp.api.NoticeCreateResponse
import com.example.dormapp.api.RetrofitClient
import com.example.dormapp.databinding.FragmentNoticeCreateBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NoticeCreateFragment : Fragment() {

    private var _binding: FragmentNoticeCreateBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoticeCreateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSubmitNotice.setOnClickListener {
            val title = binding.etNoticeTitle.text.toString().trim()
            val content = binding.etNoticeContent.text.toString().trim()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "제목과 내용을 모두 입력하세요.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val req = NoticeCreateRequest(title = title, content = content)
            val api = RetrofitClient.create(requireContext())
            api.addNotice(req)
                .enqueue(object : Callback<NoticeCreateResponse> {
                    override fun onResponse(
                        call: Call<NoticeCreateResponse>,
                        response: Response<NoticeCreateResponse>
                    ) {
                        val body = response.body()
                        if (response.isSuccessful && body?.success == true) {
                            Toast.makeText(
                                requireContext(),
                                "공지 등록 성공!",
                                Toast.LENGTH_SHORT
                            ).show()
                            findNavController()
                                .navigate(R.id.action_noticeCreateFragment_to_noticeFragment)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                body?.error ?: "등록 실패: ${response.code()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(
                        call: Call<NoticeCreateResponse>,
                        t: Throwable
                    ) {
                        Toast.makeText(
                            requireContext(),
                            "네트워크 오류: ${t.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
