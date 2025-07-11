package com.example.dormapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.fragment.findNavController
import com.example.dormapp.adapter.PostAdapter
import com.example.dormapp.api.LikeResponse
import com.example.dormapp.api.PostData
import com.example.dormapp.api.PostListResponse
import com.example.dormapp.api.RetrofitClient
import com.example.dormapp.databinding.FragmentPostListBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostListFragment : Fragment() {

    private var _binding: FragmentPostListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PostAdapter
    private val items = mutableListOf<PostData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PostAdapter(
            items,
            onClick = { post ->
                val action =
                    PostListFragmentDirections.actionPostListFragmentToPostDetailFragment(post.id)
                findNavController().navigate(action)
            },
            onLikeClick = { post, pos ->
                RetrofitClient.create(requireContext())
                    .toggleLike(post.id)
                    .enqueue(object : Callback<LikeResponse> {
                        override fun onResponse(
                            call: Call<LikeResponse>,
                            response: Response<LikeResponse>
                        ) {
                            if (response.isSuccessful) {
                                response.body()?.let { body ->
                                    val updated = post.copy(
                                        isLiked = body.isLiked,
                                        likeCount = body.likeCount
                                    )
                                    items[pos] = updated
                                    adapter.notifyItemChanged(pos)
                                }
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "좋아요 처리 실패",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onFailure(call: Call<LikeResponse>, t: Throwable) {
                            Toast.makeText(
                                requireContext(),
                                "네트워크 에러: ${t.localizedMessage}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            }
        )
        binding.rvPosts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPosts.adapter = adapter

        binding.fabAddPost.setOnClickListener {
            findNavController().navigate(R.id.action_postListFragment_to_postCreateFragment)
        }

        loadPosts()
    }

    private fun loadPosts() {
        RetrofitClient.create(requireContext())
            .getPosts()
            .enqueue(object : Callback<PostListResponse> {
                override fun onResponse(
                    call: Call<PostListResponse>, response: Response<PostListResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val posts = response.body()?.posts ?: emptyList()
                        Log.d("PostList", "posts.size = ${posts.size}")
                        items.clear()
                        items.addAll(posts)
                        adapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(requireContext(), "불러오기 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<PostListResponse>, t: Throwable) {
                    Toast.makeText(
                        requireContext(),
                        "네트워크 에러: ${t.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
