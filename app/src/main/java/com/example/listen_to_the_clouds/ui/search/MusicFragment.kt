package com.example.listen_to_the_clouds.ui.search

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.listen_to_the_clouds.MainActivity
import com.example.listen_to_the_clouds.adapter.SearchSongAdapter
import com.example.listen_to_the_clouds.databinding.FragmentMusicBinding
import com.example.listen_to_the_clouds.player.MusicPlayerManager
import com.example.listen_to_the_clouds.ui.activity.search.SearchViewModel

class MusicFragment : Fragment() {

    private var _binding: FragmentMusicBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: SearchViewModel by activityViewModels()
    private lateinit var adapter: SearchSongAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMusicBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = SearchSongAdapter { song ->
            onSongClick(song)
        }
        
        binding.songList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MusicFragment.adapter
        }
    }
    
    /**
     * 处理歌曲点击事件
     */
    private fun onSongClick(song: com.example.listen_to_the_clouds.data.model.HomeSong) {
        // 获取当前搜索结果列表
        val currentList = adapter.currentList
        
        // 查找点击歌曲的索引
        val index = currentList.indexOfFirst { it.id == song.id }
        
        if (index != -1 && currentList.isNotEmpty()) {
            // 设置播放列表并播放指定歌曲
            MusicPlayerManager.setPlaylistAndPlay(currentList, index)
            Toast.makeText(requireContext(), "开始播放: ${song.name}", Toast.LENGTH_SHORT).show()
            
            // 跳转到 MainActivity 的播放页面
            val intent = Intent(requireContext(), MainActivity::class.java).apply {
                // 添加标志，跳转到播放页面
                putExtra("navigate_to", "playback")
                // 清除栈顶的 Activity，避免返回时回到搜索页面
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            requireActivity().finish()
        } else {
            Toast.makeText(requireContext(), "播放失败，请稍后重试", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        // 初始状态显示空状态视图
        binding.emptyView.visibility = View.VISIBLE
        binding.songList.visibility = View.GONE
        
        viewModel.songSearchResults.observe(viewLifecycleOwner) { songs ->
            adapter.submitList(songs)
            
            // 根据搜索结果显示或隐藏空状态
            if (songs.isEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
                binding.songList.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.GONE
                binding.songList.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}