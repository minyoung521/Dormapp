package com.example.dormapp

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment

class MenuFragment : Fragment(R.layout.fragment_menu) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Toast.makeText(requireContext(), "식단표 입니다!", Toast.LENGTH_SHORT).show()
    }
}
