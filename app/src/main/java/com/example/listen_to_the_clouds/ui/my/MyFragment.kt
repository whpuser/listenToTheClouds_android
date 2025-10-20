package com.example.listen_to_the_clouds.ui.my

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.listen_to_the_clouds.databinding.FragmentMyBinding

class MyFragment : Fragment(), View.OnClickListener {

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

        binding.logIn.setOnClickListener(this)
        binding.collectMusic.setOnClickListener(this)
        binding.collectPlatList.setOnClickListener(this)
        binding.createPlatList.setOnClickListener(this)
        binding.userFeedback.setOnClickListener(this)
        binding.uploadAudio.setOnClickListener(this)
    }

    private fun initObserve() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        val index = 0
        Log.i("TAG", "MY页面叠加: ${index+1}")
        _binding = null
    }

    override fun onClick(v: View?) {
        when(v){
            binding.logIn->{

            }
        }

    }
}