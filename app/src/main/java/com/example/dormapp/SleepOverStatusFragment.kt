package com.example.dormapp

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dormapp.adapter.SleepOverStatusAdapter
import com.example.dormapp.api.DeleteResponse
import com.example.dormapp.api.OutingApplyResponse
import com.example.dormapp.api.RetrofitClient
import com.example.dormapp.api.SleepOverStatusResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SleepOverStatusFragment : Fragment(R.layout.fragment_sleepover_status) {

    private lateinit var recycler: RecyclerView
    private val api by lazy { RetrofitClient.create(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler = view.findViewById(R.id.recyclerStatus)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        loadData()
    }

    private fun loadData() {
        val isAdmin = requireContext()
            .getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .getBoolean("is_staff", false)

        api.getSleepOverStatus().enqueue(object : Callback<SleepOverStatusResponse> {
            override fun onResponse(
                call: Call<SleepOverStatusResponse>,
                response: Response<SleepOverStatusResponse>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val list = response.body()!!.list.orEmpty()
                    recycler.adapter = SleepOverStatusAdapter(
                        items     = list,
                        isAdmin   = isAdmin,
                        onApprove = { approveOuting(it) },
                        onReject  = { rejectOuting(it) }
                    )
                    if (list.isEmpty()) {
                        Toast.makeText(requireContext(), "외박 신청 내역이 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "현황 불러오기 실패: 코드 ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<SleepOverStatusResponse>, t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "서버 연결 실패: ${t.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun approveOuting(id: Int) {
        api.approveOuting(id).enqueue(object : Callback<OutingApplyResponse> {
            override fun onResponse(
                call: Call<OutingApplyResponse>,
                response: Response<OutingApplyResponse>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(requireContext(), "승인되었습니다.", Toast.LENGTH_SHORT).show()
                    loadData()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "승인 실패: 코드 ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<OutingApplyResponse>, t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "서버 연결 실패: ${t.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun rejectOuting(id: Int) {
        api.rejectOuting(id).enqueue(object : Callback<DeleteResponse> {
            override fun onResponse(
                call: Call<DeleteResponse>,
                response: Response<DeleteResponse>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(requireContext(), "거절되었습니다.", Toast.LENGTH_SHORT).show()
                    loadData()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "거절 실패: 코드 ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<DeleteResponse>, t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "서버 연결 실패: ${t.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
