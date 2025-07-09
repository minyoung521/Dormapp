package com.example.dormapp

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dormapp.api.DormApplyListItem
import com.example.dormapp.api.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.awaitResponse

class DormAdminFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DormAdminAdapter
    private var itemList: List<DormApplyListItem> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dorm_admin, container, false)
        recyclerView = view.findViewById(R.id.dormRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = DormAdminAdapter(emptyList()) { item ->
            showDetailDialog(item)
        }
        recyclerView.adapter = adapter

        fetchDormApplyList()

        return view
    }

    private fun fetchDormApplyList() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.create(requireContext()).getDormApplyList().awaitResponse()
                if (response.isSuccessful && response.body()?.success == true) {
                    itemList = response.body()?.dorms ?: emptyList()
                    adapter.updateList(itemList)
                } else {
                    Toast.makeText(requireContext(), "신청자 불러오기 실패!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "네트워크 오류: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDetailDialog(item: DormApplyListItem) {
        val msg = "이름: ${item.name}\n" +
                "학번: ${item.student_number}\n" +
                "성별: ${item.gender}\n" +
                "문의: ${item.content}\n" +
                "빌딩: ${item.building_name ?: "-"}\n" +
                "호실: ${item.r_number ?: "-"}\n" +
                "포지션: ${item.position ?: "-"}"

        AlertDialog.Builder(requireContext())
            .setTitle("신청자 상세")
            .setMessage(msg)
            .setPositiveButton("수정/배정") { _, _ ->
                showEditDialog(item)
            }
            .setNegativeButton("삭제") { _, _ ->
                confirmDelete(item)
            }
            .setNeutralButton("닫기", null)
            .show()
    }

    private fun showEditDialog(item: DormApplyListItem) {
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 10)
        }

        val etBuilding = EditText(context).apply {
            hint = "빌딩명"
            setText(item.building_name ?: "")
        }
        val etRoom = EditText(context).apply {
            hint = "호실"
            setText(item.r_number?.toString() ?: "")
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        val etPosition = EditText(context).apply {
            hint = "포지션"
            setText(item.position?.toString() ?: "")
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        layout.addView(etBuilding)
        layout.addView(etRoom)
        layout.addView(etPosition)

        AlertDialog.Builder(context)
            .setTitle("배정/수정")
            .setView(layout)
            .setPositiveButton("확인") { _, _ ->
                updateDormApply(
                    id = item.id,
                    building = etBuilding.text.toString(),
                    room = etRoom.text.toString(),
                    position = etPosition.text.toString()
                )
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun updateDormApply(id: Int, building: String, room: String, position: String) {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.create(requireContext())
                val body = mapOf(
                    "building_name" to building,
                    "r_number" to room,
                    "position" to position
                )
                val response = api.updateDormApply(id, body).awaitResponse()
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(requireContext(), "수정/배정 완료!", Toast.LENGTH_SHORT).show()
                    fetchDormApplyList()
                } else {
                    Toast.makeText(requireContext(), "수정/배정 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "네트워크 오류: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmDelete(item: DormApplyListItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("삭제 확인")
            .setMessage("정말 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                deleteDormApply(item.id)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun deleteDormApply(id: Int) {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.create(requireContext())
                val response = api.deleteDormApply(id).awaitResponse()
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(requireContext(), "삭제 완료", Toast.LENGTH_SHORT).show()
                    fetchDormApplyList()
                } else {
                    Toast.makeText(requireContext(), "삭제 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "네트워크 오류: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
