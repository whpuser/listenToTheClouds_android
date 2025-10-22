package com.example.listen_to_the_clouds.ui.search

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.listen_to_the_clouds.adapter.SearchPlaylistAdapter
import com.example.listen_to_the_clouds.databinding.FragmentPlaylistBinding
import com.example.listen_to_the_clouds.ui.activity.playlist.PlaylistActivity
import com.example.listen_to_the_clouds.ui.activity.search.SearchViewModel

class PlaylistFragment : Fragment() {

    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: SearchViewModel by activityViewModels()
    private lateinit var adapter: SearchPlaylistAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = SearchPlaylistAdapter { playlist ->
            // 点击歌单，跳转到歌单详情页面
            val intent = Intent(requireContext(), PlaylistActivity::class.java).apply {
                putExtra("playlistId", playlist.playlistId)
            }
            startActivity(intent)
        }
        
        binding.playlistList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@PlaylistFragment.adapter
        }
    }

    private fun observeViewModel() {
        // 初始状态显示空状态视图
        binding.emptyView.visibility = View.VISIBLE
        binding.playlistList.visibility = View.GONE
        
        viewModel.playlistSearchResults.observe(viewLifecycleOwner) { playlists ->
            adapter.submitList(playlists)
            
            // 根据搜索结果显示或隐藏空状态
            if (playlists.isEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
                binding.playlistList.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.GONE
                binding.playlistList.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}