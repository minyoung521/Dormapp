package com.example.dormapp

import android.app.AlertDialog
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
import androidx.navigation.fragment.findNavController
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

    private var postId: Int = -1
    private var myUserId: Int = -1
    private var isAdmin: Boolean = false
    private var authorId: Int = -1
    private var imageUrl: String? = null

    private lateinit var commentAdapter: CommentAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPostDetailBinding.bind(view)

        postId = arguments?.getInt("postId") ?: -1

        binding.btnLike.setOnClickListener { toggleLike() }

        binding.btnSendComment.setOnClickListener {
            val text = binding.etComment.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(requireContext(), "댓글을 입력하세요.", Toast.LENGTH_SHORT).show()
            } else {
                addComment(text)
            }
        }

        binding.btnEditPost.setOnClickListener {
            val action = PostDetailFragmentDirections.actionPostDetailFragmentToPostEditFragment(
                postId = postId,
                title = binding.tvDetailTitle.text.toString(),
                content = binding.tvDetailContent.text.toString(),
                imageUrl = imageUrl
            )
            findNavController().navigate(action)
        }

        binding.btnDeletePost.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("삭제 확인")
                .setMessage("정말 삭제하시겠습니까?")
                .setPositiveButton("삭제") { _, _ -> deletePost(postId) }
                .setNegativeButton("취소", null)
                .show()
        }

        loadMyPage()
    }

    private fun loadMyPage() {
        RetrofitClient.create(requireContext())
            .getMyPage()
            .enqueue(object : Callback<MyPageResponse> {
                override fun onResponse(call: Call<MyPageResponse>, response: Response<MyPageResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val user = response.body()!!.mypage!!.user
                        myUserId = user.userId
                        isAdmin = user.isStaff
                        loadPostDetail()
                    }
                }

                override fun onFailure(call: Call<MyPageResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "유저 정보 불러오기 오류", Toast.LENGTH_SHORT).show()
                }
            })
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
                        authorId = post.authorId
                        imageUrl = post.imageUrl

                        binding.tvDetailTitle.text = post.title
                        binding.tvDetailContent.text = post.content

                        if (!imageUrl.isNullOrEmpty()) {
                            binding.ivDetailImage.visibility = View.VISIBLE
                            val url = if (imageUrl!!.startsWith("http")) imageUrl!!
                            else RetrofitClient.BASE_URL.trimEnd('/') + imageUrl!!

                            Glide.with(this@PostDetailFragment).load(url).into(binding.ivDetailImage)

                            binding.ivDetailImage.setOnClickListener {
                                showImagePreviewDialog(url)
                            }
                        } else {
                            binding.ivDetailImage.visibility = View.GONE
                        }

                        // 어댑터 초기화 및 연결
                        commentAdapter = CommentAdapter(myUserId, isAdmin) { commentId ->
                            deleteComment(commentId)
                        }
                        binding.rvComments.layoutManager = LinearLayoutManager(requireContext())
                        binding.rvComments.adapter = commentAdapter
                        commentAdapter.updateList(post.comments.orEmpty())

                        binding.tvLikeCount.text = post.likeCount.toString()
                        binding.btnLike.text = if (post.isLiked) "💖 취소" else "🤍 좋아요"

                        if (authorId == myUserId || isAdmin) {
                            binding.btnEditPost.visibility = View.VISIBLE
                            binding.btnDeletePost.visibility = View.VISIBLE
                        } else {
                            binding.btnEditPost.visibility = View.GONE
                            binding.btnDeletePost.visibility = View.GONE
                        }

                        binding.tvCommentCount.text = "댓글 ${post.comments?.size ?: 0}개"
                    }
                }

                override fun onFailure(call: Call<PostDetailResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "게시글 불러오기 오류", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun toggleLike() {
        RetrofitClient.create(requireContext())
            .toggleLike(postId)
            .enqueue(object : Callback<LikeResponse> {
                override fun onResponse(call: Call<LikeResponse>, response: Response<LikeResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val likeResponse = response.body()!!
                        binding.tvLikeCount.text = likeResponse.likeCount.toString()
                        binding.btnLike.text = if (likeResponse.isLiked) "💖 취소" else "🤍 좋아요"
                    } else {
                        Toast.makeText(requireContext(), "좋아요 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LikeResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun addComment(content: String) {
        RetrofitClient.create(requireContext())
            .addComment(postId, mapOf("content" to content))
            .enqueue(object : Callback<CommentResponse> {
                override fun onResponse(call: Call<CommentResponse>, response: Response<CommentResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        binding.etComment.text.clear()
                        loadPostDetail()
                        Toast.makeText(requireContext(), "댓글이 등록되었습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "댓글 등록 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<CommentResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun deleteComment(commentId: Int) {
        RetrofitClient.create(requireContext())
            .deleteComment(commentId)
            .enqueue(object : Callback<DeleteResponse> {
                override fun onResponse(call: Call<DeleteResponse>, response: Response<DeleteResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(requireContext(), "댓글 삭제됨", Toast.LENGTH_SHORT).show()
                        loadPostDetail()
                    } else {
                        Toast.makeText(requireContext(), "댓글 삭제 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<DeleteResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun deletePost(postId: Int) {
        RetrofitClient.create(requireContext())
            .deletePost(postId)
            .enqueue(object : Callback<DeleteResponse> {
                override fun onResponse(call: Call<DeleteResponse>, response: Response<DeleteResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(requireContext(), "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    } else {
                        Toast.makeText(requireContext(), "게시글 삭제 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<DeleteResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showImagePreviewDialog(url: String) {
        val dlgView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_image_preview, null)
        val iv = dlgView.findViewById<ImageView>(R.id.ivPreview)
        Glide.with(this).load(url).into(iv)

        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen).apply {
            setContentView(dlgView)
            setCancelable(true)
            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            }
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
