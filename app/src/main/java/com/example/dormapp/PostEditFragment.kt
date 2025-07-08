package com.example.dormapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.dormapp.api.PostRequest
import com.example.dormapp.api.PostCreateResponse
import com.example.dormapp.api.RetrofitClient
import com.example.dormapp.databinding.FragmentPostEditBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostEditFragment : Fragment() {

    private var _binding: FragmentPostEditBinding? = null
    private val binding get() = _binding!!
    private var postId = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPostEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.let {
            postId = it.getInt("postId")
            binding.etTitle.setText(it.getString("title"))
            binding.etContent.setText(it.getString("content"))
        }
        binding.btnSubmit.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val content = binding.etContent.text.toString().trim()
            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(requireContext(), "제목과 내용을 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            RetrofitClient.create(requireContext())
                .updatePost(postId, PostRequest(title, content))
                .enqueue(object : Callback<PostCreateResponse> {
                    override fun onResponse(call: Call<PostCreateResponse>, response: Response<PostCreateResponse>) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            Toast.makeText(requireContext(), "수정되었습니다", Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack()
                        } else {
                            val code = response.code()
                            val err = response.errorBody()?.string() ?: "no error body"
                            Toast.makeText(requireContext(), "수정 실패: $code\n$err", Toast.LENGTH_LONG).show()
                        }
                    }
                    override fun onFailure(call: Call<PostCreateResponse>, t: Throwable) {
                        Toast.makeText(requireContext(), "네트워크 오류: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
