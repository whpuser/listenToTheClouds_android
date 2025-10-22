package com.example.listen_to_the_clouds.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.listen_to_the_clouds.R
import com.example.listen_to_the_clouds.adapter.PlaylistPagingAdapter
import com.example.listen_to_the_clouds.adapter.SongPagingAdapter
import com.example.listen_to_the_clouds.data.model.HomeSong
import com.example.listen_to_the_clouds.databinding.FragmentHomeBinding
import com.example.listen_to_the_clouds.player.MusicPlayerManager
import com.example.listen_to_the_clouds.ui.activity.playlist.PlaylistActivity
import com.example.listen_to_the_clouds.utils.GlobalMessageBus
import android.widget.Toast
import com.example.listen_to_the_clouds.ui.activity.search.SearchActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel

    private lateinit var playlistAdapter: PlaylistPagingAdapter
    private lateinit var songAdapter: SongPagingAdapter
    
    // 存储当前歌曲列表
    private val currentSongList = mutableListOf<HomeSong>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        initView()
        initObserve()

        return binding.root
    }

    private fun initView() {

        binding.searchPage.setOnClickListener{
            val intent = Intent(requireContext(), SearchActivity::class.java)
            startActivity(intent)
        }

        playlistAdapter = PlaylistPagingAdapter { playlist ->
            // 点击歌单跳转到PlaylistActivity
            val intent = Intent(requireContext(), PlaylistActivity::class.java)
            intent.putExtra("playlistId", playlist.playlistId)
            startActivity(intent)
        }
        
        // 歌曲点击事件：设置播放列表并跳转到播放页面
        songAdapter = SongPagingAdapter { song ->
            onSongClick(song)
        }

        // 歌单列表
        binding.playlists.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = playlistAdapter
            setHasFixedSize(false)
        }

        // 歌曲列表
        binding.musical.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = songAdapter
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
        }

    }

    private fun initObserve() {
        lifecycleScope.launch {
            viewModel.playlistsFlow.collectLatest { data ->
                playlistAdapter.submitData(data)
            }
        }
        lifecycleScope.launch {
            viewModel.songsFlow.collectLatest { data ->
                songAdapter.submitData(data)
                // 更新当前歌曲列表
                updateCurrentSongList()
            }
        }


        // 收听全局消息并展示（Toast）
        lifecycleScope.launch {
            GlobalMessageBus.messages.collect { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 更新当前歌曲列表
     */
    private fun updateCurrentSongList() {
        currentSongList.clear()
        for (i in 0 until songAdapter.itemCount) {
            songAdapter.peek(i)?.let { song ->
                currentSongList.add(song)
            }
        }
    }

    /**
     * 处理歌曲点击事件
     */
    private fun onSongClick(song: HomeSong) {
        // 更新歌曲列表
        updateCurrentSongList()
        
        // 查找点击歌曲的索引
        val index = currentSongList.indexOfFirst { it.id == song.id }
        
        if (index != -1 && currentSongList.isNotEmpty()) {
            // 设置播放列表并播放指定歌曲
            MusicPlayerManager.setPlaylistAndPlay(currentSongList, index)
            
            // 跳转到播放页面
            findNavController().navigate(R.id.navigation_dashboard)
        } else {
            Toast.makeText(requireContext(), "播放失败，请稍后重试", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}





