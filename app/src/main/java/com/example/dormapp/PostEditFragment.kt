package com.example.dormapp

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.dormapp.api.PostCreateResponse
import com.example.dormapp.api.RetrofitClient
import com.example.dormapp.databinding.FragmentPostEditBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class PostEditFragment : Fragment() {

    private var _binding: FragmentPostEditBinding? = null
    private val binding get() = _binding!!

    private var postId = -1
    private var selectedImageUri: Uri? = null
    private var existingImageUrl: String? = null

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImageUri = uri
        binding.ivSelectedImage.setImageURI(uri)
        binding.ivSelectedImage.visibility = View.VISIBLE
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPostEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // argument 전달 받기
        arguments?.let {
            postId = it.getInt("postId")
            binding.etTitle.setText(it.getString("title"))
            binding.etContent.setText(it.getString("content"))
            existingImageUrl = it.getString("imageUrl")

            if (!existingImageUrl.isNullOrEmpty()) {
                val url = if (existingImageUrl!!.startsWith("http")) existingImageUrl
                else RetrofitClient.BASE_URL.trimEnd('/') + existingImageUrl
                Glide.with(this)
                    .load(url)
                    .into(binding.ivSelectedImage)
                binding.ivSelectedImage.visibility = View.VISIBLE
            }
        }

        binding.btnPickImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.btnSubmit.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val content = binding.etContent.text.toString().trim()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(requireContext(), "제목과 내용을 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            updatePostWithImage(title, content, selectedImageUri)
        }
    }

    private fun updatePostWithImage(title: String, content: String, imageUri: Uri?) {
        val svc = RetrofitClient.create(requireContext())

        val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
        val contentPart = content.toRequestBody("text/plain".toMediaTypeOrNull())

        var imagePart: MultipartBody.Part? = null

        imageUri?.let {
            val file = File(requireContext().cacheDir, "edit_upload.jpg")
            requireContext().contentResolver.openInputStream(it)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            val reqFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            imagePart = MultipartBody.Part.createFormData("image", file.name, reqFile)
        }

        svc.updatePostWithImage(postId, titlePart, contentPart, imagePart)
            .enqueue(object : Callback<PostCreateResponse> {
                override fun onResponse(call: Call<PostCreateResponse>, response: Response<PostCreateResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(requireContext(), "수정되었습니다", Toast.LENGTH_SHORT).show()
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    } else {
                        Toast.makeText(requireContext(), "수정 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<PostCreateResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
