package com.example.dormapp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.dormapp.api.CommentResponse
import com.example.dormapp.api.DeleteResponse
import com.example.dormapp.api.PostDetailResponse
import com.example.dormapp.api.RetrofitClient
import com.example.dormapp.adapter.CommentAdapter
import com.example.dormapp.databinding.FragmentPostDetailBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostDetailFragment : Fragment() {

    private var _binding: FragmentPostDetailBinding? = null
    private val binding get() = _binding!!
    private val commentAdapter = CommentAdapter()
    private var postId: Int = -1
    private val prefs by lazy { requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE) }
    private val currentUserId: Int
        get() = prefs.getInt("user_id", -1)
    private val isAdmin: Boolean
        get() = prefs.getBoolean("is_staff", false) || prefs.getBoolean("is_superuser", false)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPostDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postId = arguments?.getInt("postId") ?: -1

        binding.rvComments.layoutManager = LinearLayoutManager(requireContext())
        binding.rvComments.adapter = commentAdapter

        binding.btnEditPost.visibility = View.GONE
        binding.btnDeletePost.visibility = View.GONE

        loadPostDetail()

        binding.btnSendComment.setOnClickListener {
            val text = binding.etComment.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(requireContext(), "댓글을 입력하세요.", Toast.LENGTH_SHORT).show()
            } else {
                addComment(text)
            }
        }

        binding.btnEditPost.setOnClickListener {
            findNavController().navigate(
                R.id.action_postDetailFragment_to_postEditFragment,
                Bundle().apply {
                    putInt("postId", postId)
                    putString("title", binding.tvDetailTitle.text.toString())
                    putString("content", binding.tvDetailContent.text.toString())
                }
            )
        }

        binding.btnDeletePost.setOnClickListener {
            RetrofitClient.create(requireContext())
                .deletePost(postId)
                .enqueue(object : Callback<DeleteResponse> {
                    override fun onResponse(call: Call<DeleteResponse>, response: Response<DeleteResponse>) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            Toast.makeText(requireContext(), "삭제 완료!", Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack()
                        } else {
                            Toast.makeText(requireContext(), "삭제 실패", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<DeleteResponse>, t: Throwable) {
                        Toast.makeText(requireContext(), "네트워크 오류", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun loadPostDetail() {
        RetrofitClient.create(requireContext())
            .getPost(postId)
            .enqueue(object : Callback<PostDetailResponse> {
                override fun onResponse(call: Call<PostDetailResponse>, response: Response<PostDetailResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val post = response.body()!!.post!!

                        binding.tvDetailTitle.text = post.title
                        binding.tvDetailContent.text = post.content

                        if (!post.imageUrl.isNullOrEmpty()) {
                            binding.ivDetailImage.visibility = View.VISIBLE
                            val url = if (post.imageUrl.startsWith("http")) post.imageUrl
                            else RetrofitClient.BASE_URL.trimEnd('/') + post.imageUrl
                            Glide.with(this@PostDetailFragment)
                                .load(url)
                                .into(binding.ivDetailImage)
                        } else {
                            binding.ivDetailImage.visibility = View.GONE
                        }

                        commentAdapter.updateList(post.comments ?: emptyList())

                        if (post.authorId == currentUserId || isAdmin) {
                            binding.btnEditPost.visibility = View.VISIBLE
                            binding.btnDeletePost.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onFailure(call: Call<PostDetailResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "불러오기 오류", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun addComment(content: String) {
        RetrofitClient.create(requireContext())
            .addComment(postId, mapOf("content" to content))
            .enqueue(object : Callback<CommentResponse> {
                override fun onResponse(call: Call<CommentResponse>, response: Response<CommentResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        binding.etComment.setText("")
                        loadPostDetail()
                    } else {
                        Toast.makeText(requireContext(), "댓글 등록 실패", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<CommentResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
