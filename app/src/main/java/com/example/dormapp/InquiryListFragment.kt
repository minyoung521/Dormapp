package com.example.dormapp

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dormapp.api.InquiryListResponse
import com.example.dormapp.api.RetrofitClient
import com.example.dormapp.adapter.InquiryAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InquiryListFragment : Fragment(R.layout.fragment_inquiry_list) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fabWrite = view.findViewById<FloatingActionButton>(R.id.fab_inquiry_write)
        val prefs   = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val token   = prefs.getString("auth_token", null)
        val isStaff = prefs.getBoolean("is_staff", false)

        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }
        fabWrite.visibility = if (isStaff) View.GONE else View.VISIBLE
        fabWrite.setOnClickListener {
            findNavController().navigate(
                R.id.action_inquiryListFragment_to_inquiryWriteFragment
            )
        }

        val rv = view.findViewById<RecyclerView>(R.id.rv_inquiry_list)
        val adapter = InquiryAdapter { inquiryId ->
            val action = InquiryListFragmentDirections
                .actionInquiryListFragmentToInquiryDetailFragment(inquiryId)
            findNavController().navigate(action)
        }
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        RetrofitClient.create(requireContext())
            .getInquiries()
            .enqueue(object : Callback<InquiryListResponse> {
                override fun onResponse(
                    call: Call<InquiryListResponse>,
                    response: Response<InquiryListResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        response.body()?.inquiries?.let { adapter.updateList(it) }
                            ?: Toast.makeText(requireContext(),
                                "불러올 문의사항이 없습니다.",
                                Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(),
                            "문의사항 불러오기 실패",
                            Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<InquiryListResponse>, t: Throwable) {
                    Toast.makeText(requireContext(),
                        "네트워크 오류: ${t.localizedMessage}",
                        Toast.LENGTH_SHORT).show()
                }
            })
    }
}
