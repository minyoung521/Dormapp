package com.example.dormapp

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.dormapp.api.LoginRequest
import com.example.dormapp.api.LoginResponse
import com.example.dormapp.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginFragment : Fragment(R.layout.fragment_login) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etUsername = view.findViewById<EditText>(R.id.etUsername)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val btnLogin   = view.findViewById<Button>(R.id.btnLogin)
        val btnSignup  = view.findViewById<Button>(R.id.btnSignup)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "아이디와 비밀번호를 모두 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            RetrofitClient.create(requireContext())
                .login(LoginRequest(username, password))
                .enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        val body = response.body()
                        if (response.isSuccessful && body?.success == true) {
                            val prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
                            prefs.edit()
                                .putInt   ("user_id",      body.userId!!)
                                .putString("auth_token",   body.token)
                                .putString("username",     username)
                                .putBoolean("is_staff",     body.isStaff)
                                .putBoolean("is_superuser", body.isSuperuser)
                                .apply()
                            findNavController().navigate(R.id.action_login_to_main)
                        } else {
                            Toast.makeText(requireContext(),
                                body?.error ?: "로그인 실패 (${response.code()})",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        Toast.makeText(requireContext(),
                            "네트워크 에러: ${t.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }

        btnSignup.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_signup)
        }
    }
}
