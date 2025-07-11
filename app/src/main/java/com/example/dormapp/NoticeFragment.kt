package com.example.dormapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dormapp.adapter.NoticeAdapter
import com.example.dormapp.api.NoticeCreateResponse
import com.example.dormapp.api.NoticeListResponse
import com.example.dormapp.api.RetrofitClient
import com.example.dormapp.databinding.FragmentNoticeBinding
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class NoticeFragment : Fragment(R.layout.fragment_notice) {

    private var _binding: FragmentNoticeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: NoticeAdapter

    private var dialogImagePreview: ImageView? = null
    private var noticeImageUri: Uri? = null

    private val noticeImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                noticeImageUri = result.data?.data
                dialogImagePreview?.apply {
                    setImageURI(noticeImageUri)
                    visibility = View.VISIBLE
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoticeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvNotices.layoutManager = LinearLayoutManager(requireContext())
        adapter = NoticeAdapter(mutableListOf()) { notice ->
            val bundle = Bundle().apply {
                putString("title", notice.title)
                putString("content", notice.content)
                putString("imageUrl", notice.imageUrl ?: "")
            }
            findNavController().navigate(
                R.id.action_noticeFragment_to_noticeDetailFragment,
                bundle
            )
        }
        binding.rvNotices.adapter = adapter

        val prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val isStaff = prefs.getBoolean("is_staff", false)
        binding.fabAddNotice.visibility = if (isStaff) View.VISIBLE else View.GONE

        binding.fabAddNotice.setOnClickListener {
            showCreateDialog()
        }

        loadNotices()
    }

    private fun showCreateDialog() {
        val ctx = requireContext()
        val titleInput = EditText(ctx).apply { hint = "제목" }
        val contentInput = EditText(ctx).apply { hint = "내용" }
        val pickButton = Button(ctx).apply { text = "이미지 첨부" }
        dialogImagePreview = ImageView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                300
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
            visibility = View.GONE
        }

        pickButton.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            noticeImageLauncher.launch(intent)
        }

        val container = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 16, 32, 16)
            addView(titleInput)
            addView(contentInput)
            addView(pickButton)
            addView(dialogImagePreview)
        }

        AlertDialog.Builder(ctx)
            .setTitle("공지 작성")
            .setView(container)
            .setPositiveButton("등록") { _, _ ->
                val title = titleInput.text.toString().trim()
                val content = contentInput.text.toString().trim()
                if (title.isEmpty() || content.isEmpty()) {
                    Toast.makeText(ctx, "제목과 내용을 모두 입력하세요.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                postNoticeWithImage(title, content, noticeImageUri)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun postNoticeWithImage(title: String, content: String, imageUri: Uri?) {
        val api = RetrofitClient.create(requireContext())
        val titleRb = title.toRequestBody("text/plain".toMediaType())
        val contentRb = content.toRequestBody("text/plain".toMediaType())
        var imagePart: MultipartBody.Part? = null

        imageUri?.let { uri ->
            val file = File(requireContext().cacheDir, "notice.jpg")
            requireContext().contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            val reqFile = file.asRequestBody("image/*".toMediaType())
            imagePart = MultipartBody.Part.createFormData("image", file.name, reqFile)
        }

        api.addNoticeWithImage(titleRb, contentRb, imagePart)
            .enqueue(object : Callback<NoticeCreateResponse> {
                override fun onResponse(
                    call: Call<NoticeCreateResponse>,
                    response: Response<NoticeCreateResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(requireContext(), "공지 등록 완료", Toast.LENGTH_SHORT).show()
                        loadNotices()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "등록 실패: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<NoticeCreateResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadNotices() {
        RetrofitClient.create(requireContext())
            .getNotices()
            .enqueue(object : Callback<NoticeListResponse> {
                override fun onResponse(
                    call: Call<NoticeListResponse>,
                    response: Response<NoticeListResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        adapter.updateList(response.body()!!.notices.orEmpty())
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "공지 불러오기 실패: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<NoticeListResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "네트워크 에러", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
