package com.example.dormapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.dormapp.api.PostCreateResponse
import com.example.dormapp.api.RetrofitClient
import com.example.dormapp.databinding.FragmentPostCreateBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class PostCreateFragment : Fragment(R.layout.fragment_post_create) {
    private var _binding: FragmentPostCreateBinding? = null
    private val binding get() = _binding!!
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                selectedImageUri = data?.data
                binding.ivSelectedImage.apply {
                    setImageURI(selectedImageUri)
                    visibility = View.VISIBLE
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentPostCreateBinding.bind(view)

        binding.btnPickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        }

        binding.btnSubmit.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val content = binding.etContent.text.toString().trim()
            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(requireContext(), "제목과 내용을 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            uploadPost(title, content, selectedImageUri)
        }
    }

    private fun uploadPost(title: String, content: String, imageUri: Uri?) {
        val svc = RetrofitClient.create(requireContext())

        val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
        val contentPart = content.toRequestBody("text/plain".toMediaTypeOrNull())

        var imagePart: MultipartBody.Part? = null
        imageUri?.let {
            val file = File(requireContext().cacheDir, "upload.jpg")
            requireContext().contentResolver.openInputStream(it)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            val reqFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            imagePart = MultipartBody.Part.createFormData("image", file.name, reqFile)
        }

        svc.addPostWithImage(titlePart, contentPart, imagePart).enqueue(postCallback())
    }

    private fun postCallback() = object : Callback<PostCreateResponse> {
        override fun onResponse(call: Call<PostCreateResponse>, response: Response<PostCreateResponse>) {
            if (response.isSuccessful && response.body()?.success == true) {
                Toast.makeText(requireContext(), "등록되었습니다", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            } else {
                Toast.makeText(requireContext(), "실패 ${response.code()}", Toast.LENGTH_SHORT).show()
            }
        }
        override fun onFailure(call: Call<PostCreateResponse>, t: Throwable) {
            Toast.makeText(requireContext(), "네트워크 오류", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
