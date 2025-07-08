package com.example.dormapp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dormapp.adapter.NoticeAdapter
import com.example.dormapp.api.NoticeCreateRequest
import com.example.dormapp.api.NoticeCreateResponse
import com.example.dormapp.api.NoticeListResponse
import com.example.dormapp.api.RetrofitClient
import com.example.dormapp.databinding.FragmentNoticeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NoticeFragment : Fragment(R.layout.fragment_notice) {

    private var _binding: FragmentNoticeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: NoticeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoticeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvNotices.layoutManager = LinearLayoutManager(requireContext())
        adapter = NoticeAdapter(mutableListOf()) { notice ->
            val bundle = Bundle().apply {
                putString("title", notice.title)
                putString("content", notice.content)
            }
            findNavController().navigate(
                R.id.action_noticeFragment_to_noticeDetailFragment,
                bundle
            )
        }
        binding.rvNotices.adapter = adapter

        val prefs = requireContext()
            .getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val isStaff = prefs.getBoolean("is_staff", false)
        binding.fabAddNotice.visibility = if (isStaff) View.VISIBLE else View.GONE

        val api = RetrofitClient.create(requireContext())


        fun loadNotices() {
            api.getNotices().enqueue(object : Callback<NoticeListResponse> {
                override fun onResponse(
                    call: Call<NoticeListResponse>,
                    response: Response<NoticeListResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        adapter.updateList(response.body()!!.notices ?: emptyList())
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "공지 불러오기 실패: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<NoticeListResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "네트워크 에러", Toast.LENGTH_SHORT).show()
                }
            })
        }
        loadNotices()


        binding.fabAddNotice.setOnClickListener {
            val ctx = requireContext()
            val titleInput = EditText(ctx).apply {
                hint = "제목"
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            val contentInput = EditText(ctx).apply {
                hint = "내용"
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            val container = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(32, 16, 32, 16)
                addView(titleInput)
                addView(contentInput)
            }

            AlertDialog.Builder(ctx)
                .setTitle("공지 작성")
                .setView(container)
                .setPositiveButton("등록") { _, _ ->
                    val title = titleInput.text.toString().trim()
                    val content = contentInput.text.toString().trim()
                    if (title.isEmpty() || content.isEmpty()) {
                        Toast.makeText(ctx, "제목과 내용을 모두 입력하세요", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    api.addNotice(NoticeCreateRequest(title, content))
                        .enqueue(object : Callback<NoticeCreateResponse> {
                            override fun onResponse(
                                call: Call<NoticeCreateResponse>,
                                response: Response<NoticeCreateResponse>
                            ) {
                                if (response.isSuccessful && response.body()?.success == true) {
                                    Toast.makeText(ctx, "등록 성공", Toast.LENGTH_SHORT).show()
                                    loadNotices()
                                } else {
                                    Toast.makeText(ctx, "등록 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                                }
                            }
                            override fun onFailure(call: Call<NoticeCreateResponse>, t: Throwable) {
                                Toast.makeText(ctx, "네트워크 에러", Toast.LENGTH_SHORT).show()
                            }
                        })
                }
                .setNegativeButton("취소", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
