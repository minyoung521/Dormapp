package com.example.dormapp

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.dormapp.api.RetrofitClient
import com.example.dormapp.databinding.FragmentNoticeDetailBinding

class NoticeDetailFragment : Fragment(R.layout.fragment_notice_detail) {

    private var _binding: FragmentNoticeDetailBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentNoticeDetailBinding.bind(view)

        val title    = arguments?.getString("title")
        val content  = arguments?.getString("content")
        val imageUrl = arguments?.getString("imageUrl")

        binding.tvDetailTitle.text   = title
        binding.tvDetailContent.text = content

        if (!imageUrl.isNullOrEmpty()) {
            val fullUrl = RetrofitClient.BASE_URL.trimEnd('/') + imageUrl
            binding.ivDetailImage.visibility = View.VISIBLE
            Glide.with(this)
                .load(fullUrl)
                .into(binding.ivDetailImage)

            binding.ivDetailImage.setOnClickListener {
                showImagePreview(fullUrl)
            }
        }
    }

    private fun showImagePreview(url: String) {
        val dlgView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_image_preview, null)
        val ivPreview = dlgView.findViewById<ImageView>(R.id.ivPreview)

        Glide.with(this)
            .load(url)
            .into(ivPreview)

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
