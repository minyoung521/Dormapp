package com.example.dormapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class MainFragment : Fragment(R.layout.fragment_main) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Toast.makeText(requireContext(), "로그인이 완료되었습니다!", Toast.LENGTH_SHORT).show()

        view.findViewById<ImageView>(R.id.imgDormitory).setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_dormFragment)
        }

        view.findViewById<ImageView>(R.id.imgOuting).setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_sleepoverFragment)
        }

        view.findViewById<ImageView>(R.id.imgNotice).setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_noticeFragment)
        }

        view.findViewById<LinearLayout>(R.id.btnCommunity).setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_postListFragment)
        }

        view.findViewById<LinearLayout>(R.id.btnMenu).setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_menuFragment)
        }

        view.findViewById<LinearLayout>(R.id.btnMyPage).setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_myPageFragment)
        }

        view.findViewById<LinearLayout>(R.id.btnBus).setOnClickListener {
            val url = "http://43.202.118.147:8000/bus/"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
    }
}
