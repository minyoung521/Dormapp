package com.example.dormapp

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.dormapp.api.*
import com.example.dormapp.adapter.CommentAdapter
import com.example.dormapp.databinding.FragmentPostDetailBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostDetailFragment : Fragment(R.layout.fragment_post_detail) {

    private var _binding: FragmentPostDetailBinding? = null
    private val binding get() = _binding!!
    private val commentAdapter = CommentAdapter()
    private var postId: Int = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPostDetailBinding.bind(view)

        postId = arguments?.getInt("postId") ?: -1

        binding.rvComments.layoutManager = LinearLayoutManager(requireContext())
        binding.rvComments.adapter = commentAdapter

        binding.btnLike.setOnClickListener { toggleLike() }
        binding.btnSendComment.setOnClickListener {
            val text = binding.etComment.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(requireContext(), "댓글을 입력하세요.", Toast.LENGTH_SHORT).show()
            } else {
                addComment(text)
            }
        }

        loadPostDetail()
    }

    private fun loadPostDetail() {
        RetrofitClient.create(requireContext())
            .getPost(postId)
            .enqueue(object : Callback<PostDetailResponse> {
                override fun onResponse(
                    call: Call<PostDetailResponse>,
                    response: Response<PostDetailResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val post = response.body()!!.post!!

                        binding.tvDetailTitle.text   = post.title
                        binding.tvDetailContent.text = post.content

                        if (!post.imageUrl.isNullOrEmpty()) {
                            binding.ivDetailImage.visibility = View.VISIBLE

                            val url =
                                if (post.imageUrl.startsWith("http")) post.imageUrl
                                else RetrofitClient.BASE_URL.trimEnd('/') + post.imageUrl

                            Glide.with(this@PostDetailFragment)
                                .load(url)
                                .into(binding.ivDetailImage)

                            binding.ivDetailImage.setOnClickListener {
                                showImagePreviewDialog(url)
                            }
                        } else {
                            binding.ivDetailImage.visibility = View.GONE
                        }

                        commentAdapter.updateList(post.comments.orEmpty())
                        binding.tvLikeCount.text = post.likeCount.toString()
                        binding.btnLike.text = if (post.isLiked) "💖 취소" else "🤍 좋아요"
                    }
                }
                override fun onFailure(call: Call<PostDetailResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "불러오기 오류", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showImagePreviewDialog(url: String) {
        val dlgView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_image_preview, null)

        val iv = dlgView.findViewById<ImageView>(R.id.ivPreview)
        Glide.with(this)
            .load(url)
            .into(iv)

        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen).apply {
            setContentView(dlgView)
            setCancelable(true)
            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
                )
            }
        }
        dialog.show()
    }

    private fun toggleLike() { /* ... */ }
    private fun addComment(content: String) { /* ... */ }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
