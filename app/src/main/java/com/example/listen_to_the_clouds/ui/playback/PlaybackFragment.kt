package com.example.listen_to_the_clouds.ui.playback

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.listen_to_the_clouds.databinding.FragmentDashboardBinding
import com.example.listen_to_the_clouds.ui.home.HomeViewModel

class PlaybackFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: PlaybackViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[PlaybackViewModel::class.java]
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)


        initView()
        initObserve()
        return binding.root
    }

    private fun initView() {

    }

    private fun initObserve() {
        viewModel.text.observe(viewLifecycleOwner) {
            binding.textDashboard.text = it
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}