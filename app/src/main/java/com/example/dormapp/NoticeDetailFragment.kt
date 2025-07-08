package com.example.dormapp

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.dormapp.databinding.FragmentNoticeDetailBinding

class NoticeDetailFragment : Fragment(R.layout.fragment_notice_detail) {

    private var _binding: FragmentNoticeDetailBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentNoticeDetailBinding.bind(view)

        val title = arguments?.getString("title")
        val content = arguments?.getString("content")

        binding.tvDetailTitle.text = title
        binding.tvDetailContent.text = content
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
