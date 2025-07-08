package com.example.dormapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.dormapp.api.DormApplyRequest
import com.example.dormapp.api.DormApplyResponse
import com.example.dormapp.api.RetrofitClient
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class ErrorResponse(
    val success: Boolean,
    val error: String?
)

class DormFragment : Fragment() {

    private fun mapErrorToKorean(serverErr: String?): String {
        return when (serverErr) {
            "This student number has already been applied." ->
                "이미 신청된 학번입니다."
            "Name, student number, and gender are required." ->
                "이름, 학번, 성별을 모두 입력하세요."
            else ->
                serverErr ?: "알 수 없는 오류가 발생했습니다."
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view        = inflater.inflate(R.layout.fragment_dorm, container, false)
        val etUsername  = view.findViewById<EditText>(R.id.etUsername)
        val etUsernum   = view.findViewById<EditText>(R.id.etUsernum)
        val genderGroup = view.findViewById<RadioGroup>(R.id.genderGroup)
        val etContent   = view.findViewById<EditText>(R.id.dormContent)
        val btnApply    = view.findViewById<Button>(R.id.btnApply)

        btnApply.setOnClickListener {
            val name        = etUsername.text.toString().trim()
            val studentNum  = etUsernum.text.toString().trim()
            val gender      = when (genderGroup.checkedRadioButtonId) {
                R.id.radioMale   -> "male"
                R.id.radioFemale -> "female"
                else             -> ""
            }
            val contentText = etContent.text.toString().trim()

            if (name.isEmpty() || studentNum.isEmpty() || gender.isEmpty()) {
                Toast.makeText(requireContext(), "이름, 학번, 성별을 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = DormApplyRequest(
                name           = name,
                student_number = studentNum,
                gender         = gender,
                content        = contentText
            )

            RetrofitClient.create(requireContext())
                .applyDorm(request)
                .enqueue(object : Callback<DormApplyResponse> {
                    override fun onResponse(
                        call: Call<DormApplyResponse>,
                        response: Response<DormApplyResponse>
                    ) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            Toast.makeText(requireContext(), "신청이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                            etUsername.text.clear()
                            etUsernum.text.clear()
                            genderGroup.clearCheck()
                            findNavController().navigate(R.id.action_dormFragment_to_mainFragment)
                        } else {
                            val err = response.errorBody()?.charStream()?.let {
                                Gson().fromJson(it, ErrorResponse::class.java)
                            }
                            Toast.makeText(requireContext(), mapErrorToKorean(err?.error), Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<DormApplyResponse>, t: Throwable) {
                        Toast.makeText(requireContext(), "네트워크 오류: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        return view
    }
}
