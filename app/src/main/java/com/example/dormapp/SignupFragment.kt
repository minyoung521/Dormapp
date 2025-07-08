package com.example.dormapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.dormapp.api.SignupRequest
import com.example.dormapp.api.SignupResponse
import com.example.dormapp.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignupFragment : Fragment(R.layout.fragment_signup) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etName       = view.findViewById<EditText>(R.id.et_register_name)
        val etUsername   = view.findViewById<EditText>(R.id.et_register_id)
        val etPassword   = view.findViewById<EditText>(R.id.et_register_pw)
        val etEmail      = view.findViewById<EditText>(R.id.et_register_email)
        val etDepartment = view.findViewById<EditText>(R.id.et_register_department)
        val btnSignup    = view.findViewById<Button>(R.id.btn_register_button)

        btnSignup.setOnClickListener {
            val fullName   = etName.text.toString().trim()
            val username   = etUsername.text.toString().trim()
            val password   = etPassword.text.toString().trim()
            val email      = etEmail.text.toString().trim()
            val department = etDepartment.text.toString().trim()

            if (fullName.isEmpty() ||
                username.isEmpty() ||
                password.isEmpty() ||
                email.isEmpty() ||
                department.isEmpty()
            ) {
                Toast.makeText(requireContext(),
                    "모든 값을 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val signupReq = SignupRequest(
                username   = username,
                password   = password,
                email      = email,
                department = department,
                fullName   = fullName
            )

            RetrofitClient.create(requireContext())
                .signup(signupReq)
                .enqueue(object : Callback<SignupResponse> {
                    override fun onResponse(
                        call: Call<SignupResponse>,
                        response: Response<SignupResponse>
                    ) {
                        val body = response.body()
                        if (response.isSuccessful && body?.success == true) {
                            Toast.makeText(
                                requireContext(),
                                "회원가입 완료! 로그인 해주세요.",
                                Toast.LENGTH_SHORT
                            ).show()
                            findNavController().navigate(R.id.action_signup_to_login)
                        } else {
                            val errMsg = body?.error
                                ?: response.errorBody()?.string()
                                ?: "회원가입 실패: ${response.code()}"
                            Toast.makeText(
                                requireContext(),
                                errMsg,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<SignupResponse>, t: Throwable) {
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
