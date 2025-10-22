package com.example.listen_to_the_clouds.ui.activity.favorites

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.listen_to_the_clouds.MainActivity
import com.example.listen_to_the_clouds.adapter.SearchPlaylistAdapter
import com.example.listen_to_the_clouds.adapter.SearchSongAdapter
import com.example.listen_to_the_clouds.databinding.ActivityFavoritesListBinding
import com.example.listen_to_the_clouds.player.MusicPlayerManager
import com.example.listen_to_the_clouds.ui.activity.playlist.PlaylistActivity
import kotlinx.coroutines.launch

class FavoritesListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritesListBinding
    private lateinit var viewModel: FavoritesListViewModel
    private var listType: String = TYPE_FAVORITE_SONGS
    
    private var songAdapter: SearchSongAdapter? = null
    private var playlistAdapter: SearchPlaylistAdapter? = null

    companion object {
        const val EXTRA_LIST_TYPE = "list_type"
        const val TYPE_FAVORITE_SONGS = "favorite_songs"
        const val TYPE_FAVORITE_PLAYLISTS = "favorite_playlists"
        const val TYPE_CREATED_PLAYLISTS = "created_playlists"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[FavoritesListViewModel::class.java]

        // 获取列表类型
        listType = intent.getStringExtra(EXTRA_LIST_TYPE) ?: TYPE_FAVORITE_SONGS

        initView()
        initObserve()
        loadData()
    }

    private fun initView() {
        // 设置标题
        binding.title.text = when (listType) {
            TYPE_FAVORITE_SONGS -> "收藏的歌曲"
            TYPE_FAVORITE_PLAYLISTS -> "收藏的歌单"
            TYPE_CREATED_PLAYLISTS -> "创建的歌单"
            else -> "列表"
        }

        // 返回按钮
        binding.backButton.setOnClickListener {
            finish()
        }

        // 设置RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        
        // 根据类型设置适配器
        when (listType) {
            TYPE_FAVORITE_SONGS -> {
                songAdapter = SearchSongAdapter { song ->
                    // 点击歌曲，播放歌曲并跳转到播放页面
                    val songs = viewModel.songs.value
                    val index = songs.indexOf(song)
                    if (index != -1) {
                        MusicPlayerManager.setPlaylistAndPlay(songs, index)
                        // 跳转到主页面的播放Tab
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("navigate_to", "playback")
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        startActivity(intent)
                    }
                }
                binding.recyclerView.adapter = songAdapter
            }
            TYPE_FAVORITE_PLAYLISTS, TYPE_CREATED_PLAYLISTS -> {
                playlistAdapter = SearchPlaylistAdapter { playlist ->
                    // 点击歌单，跳转到歌单详情
                    val intent = Intent(this, PlaylistActivity::class.java)
                    intent.putExtra("playlistId", playlist.playlistId)
                    startActivity(intent)
                }
                binding.recyclerView.adapter = playlistAdapter
            }
        }
    }

    private fun initObserve() {
        // 观察加载状态
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        // 观察空状态
        lifecycleScope.launch {
            viewModel.isEmpty.collect { isEmpty ->
                binding.emptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE
                binding.recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
            }
        }

        // 观察错误信息
        lifecycleScope.launch {
            viewModel.errorMessage.collect { message ->
                message?.let {
                    Toast.makeText(this@FavoritesListActivity, it, Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        // 观察歌曲列表
        lifecycleScope.launch {
            viewModel.songs.collect { songs ->
                songAdapter?.submitList(songs)
            }
        }
        
        // 观察歌单列表
        lifecycleScope.launch {
            viewModel.playlists.collect { playlists ->
                playlistAdapter?.submitList(playlists)
            }
        }
    }

    private fun loadData() {
        when (listType) {
            TYPE_FAVORITE_SONGS -> viewModel.loadFavoriteSongs()
            TYPE_FAVORITE_PLAYLISTS -> viewModel.loadFavoritePlaylists()
            TYPE_CREATED_PLAYLISTS -> viewModel.loadCreatedPlaylists()
        }
    }
}
