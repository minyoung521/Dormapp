package com.example.dormapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.dormapp.api.OutingApplyRequest
import com.example.dormapp.api.OutingApplyResponse
import com.example.dormapp.api.RetrofitClient
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class SleepoverFragment : Fragment(R.layout.fragment_sleepover) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val etName       = view.findViewById<EditText>(R.id.etName)
        val etStudentNum = view.findViewById<EditText>(R.id.etStudentNum)
        val etOutDate    = view.findViewById<EditText>(R.id.etOutDate)
        val btnApply     = view.findViewById<Button>(R.id.btnApplyOuting)

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        etOutDate.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    etOutDate.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btnApply.setOnClickListener {
            val name    = etName.text.toString().trim()
            val num     = etStudentNum.text.toString().trim()
            val outDate = etOutDate.text.toString().trim()

            if (name.isEmpty() || num.isEmpty() || outDate.isEmpty()) {
                Toast.makeText(requireContext(), "이름, 학번, 날짜를 모두 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = OutingApplyRequest(
                name           = name,
                student_number = num,
                out_date       = outDate
            )

            val api = RetrofitClient.create(requireContext())
            api.applyOuting(request).enqueue(object : Callback<OutingApplyResponse> {
                override fun onResponse(
                    call: Call<OutingApplyResponse>,
                    response: Response<OutingApplyResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(requireContext(), "외박 신청이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    } else {
                        val errJson = response.errorBody()?.charStream()
                        val err = try {
                            Gson().fromJson(errJson, OutingApplyResponse::class.java).error
                        } catch (e: Exception) {
                            null
                        }
                        Toast.makeText(requireContext(), err ?: "신청에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<OutingApplyResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "네트워크 오류: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
