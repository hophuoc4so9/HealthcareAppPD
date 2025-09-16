package com.example.healthcareapppd.presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.healthcareapppd.R

class IntroduceFragment3 : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Nối fragment_introduce3.xml vào Fragment này
        return inflater.inflate(R.layout.fragment_introduce3, container, false)
    }
}
