package com.example.dormapp

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.dormapp.api.NoticeCreateResponse
import com.example.dormapp.api.RetrofitClient
import com.example.dormapp.databinding.FragmentNoticeDetailBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class NoticeDetailFragment : Fragment(R.layout.fragment_notice_detail) {

    private var _binding: FragmentNoticeDetailBinding? = null
    private val binding get() = _binding!!

    private var noticeId: Int = -1
    private var fullImageUrl: String? = null
    private var currentTitle: String? = null
    private var currentContent: String? = null
    private var currentImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            currentImageUri = result.data?.data
            Toast.makeText(requireContext(), "이미지 선택 완료", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentNoticeDetailBinding.bind(view)

        currentTitle = arguments?.getString("title")
        currentContent = arguments?.getString("content")
        val imageUrl = arguments?.getString("imageUrl")
        noticeId = arguments?.getInt("noticeId", -1) ?: -1

        binding.tvDetailTitle.text = currentTitle
        binding.tvDetailContent.text = currentContent

        if (!imageUrl.isNullOrEmpty()) {
            fullImageUrl = RetrofitClient.BASE_URL.trimEnd('/') + imageUrl
            binding.ivDetailImage.visibility = View.VISIBLE
            Glide.with(this).load(fullImageUrl).into(binding.ivDetailImage)

            binding.ivDetailImage.setOnClickListener {
                fullImageUrl?.let { url -> showImagePreview(url) }
            }
        }

        val prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val isStaff = prefs.getBoolean("is_staff", false)

        if (isStaff) {
            binding.btnEditNotice.visibility = View.VISIBLE
            binding.btnDeleteNotice.visibility = View.VISIBLE
        }

        binding.btnDeleteNotice.setOnClickListener { confirmDeleteDialog() }
        binding.btnEditNotice.setOnClickListener { showEditDialog() }
    }

    private fun confirmDeleteDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("공지 삭제")
            .setMessage("정말로 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ -> deleteNotice() }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun deleteNotice() {
        val token = getToken()
        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "유효하지 않은 토큰", Toast.LENGTH_SHORT).show()
            return
        }

        val authHeader = "Token ${token.trim()}"
        Log.d("TOKEN_CHECK", "삭제용 토큰: $authHeader") // ✅ 로그 확인

        RetrofitClient.createWithHeader(authHeader)
            .deleteNotice(noticeId = noticeId)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "삭제 완료", Toast.LENGTH_SHORT).show()
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    } else {
                        Toast.makeText(requireContext(), "삭제 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(requireContext(), "오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showEditDialog() {
        val titleInput = EditText(requireContext()).apply {
            setText(currentTitle)
            hint = "제목"
            inputType = InputType.TYPE_CLASS_TEXT
        }

        val contentInput = EditText(requireContext()).apply {
            setText(currentContent)
            hint = "내용"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        }

        val selectImageBtn = Button(requireContext()).apply {
            text = "이미지 선택"
            setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                imagePickerLauncher.launch(intent)
            }
        }

        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 30, 40, 10)
            addView(titleInput)
            addView(contentInput)
            addView(selectImageBtn)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("공지 수정")
            .setView(layout)
            .setPositiveButton("저장") { _, _ ->
                val newTitle = titleInput.text.toString()
                val newContent = contentInput.text.toString()
                updateNoticeWithImage(newTitle, newContent)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun updateNoticeWithImage(title: String, content: String) {
        val token = getToken()
        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "유효하지 않은 토큰", Toast.LENGTH_SHORT).show()
            return
        }

        val authHeader = "Token ${token.trim()}"
        Log.d("TOKEN_CHECK", "수정용 토큰: $authHeader")

        val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
        val contentPart = content.toRequestBody("text/plain".toMediaTypeOrNull())

        val imagePart = currentImageUri?.let { uri ->
            val file = File(requireContext().cacheDir, "notice_image.jpg")
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            file.outputStream().use { output -> inputStream?.copyTo(output) }
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", file.name, requestFile)
        }

        RetrofitClient.createWithHeader(authHeader)
            .updateNoticeWithImage(
                id = noticeId,
                title = titlePart,
                content = contentPart,
                image = imagePart
            )
            .enqueue(object : Callback<NoticeCreateResponse> {
                override fun onResponse(
                    call: Call<NoticeCreateResponse>,
                    response: Response<NoticeCreateResponse>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "수정 완료", Toast.LENGTH_SHORT).show()
                        binding.tvDetailTitle.text = title
                        binding.tvDetailContent.text = content
                        currentTitle = title
                        currentContent = content
                        currentImageUri = null
                    } else {
                        Toast.makeText(requireContext(), "수정 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<NoticeCreateResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun getToken(): String? {
        val prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        return prefs.getString("auth_token", null)?.trim()
    }

    private fun showImagePreview(url: String) {
        val dlgView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_image_preview, null)
        val ivPreview = dlgView.findViewById<ImageView>(R.id.ivPreview)

        Glide.with(this).load(url).into(ivPreview)

        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen).apply {
            setContentView(dlgView)
            setCancelable(true)
        }

        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
