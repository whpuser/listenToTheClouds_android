package com.example.listen_to_the_clouds.ui.my

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.listen_to_the_clouds.databinding.FragmentMyBinding

class MyFragment : Fragment() {

    private var _binding: FragmentMyBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MyViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[MyViewModel::class.java]
        _binding = FragmentMyBinding.inflate(inflater, container, false)

        initView()
        initObserve()
        return binding.root
    }

    private fun initView() {

    }

    private fun initObserve() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}