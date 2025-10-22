package com.example.listen_to_the_clouds.ui.activity.playlist

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.listen_to_the_clouds.R
import com.example.listen_to_the_clouds.adapter.SongPagingAdapter
import com.example.listen_to_the_clouds.data.network.RESOURCE_ADDRESS
import com.example.listen_to_the_clouds.databinding.ActivityPlaylistBinding
import com.example.listen_to_the_clouds.player.MusicPlayerManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class PlaylistActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlaylistBinding
    private val viewModel: PlaylistViewModel by viewModels()
    private lateinit var songAdapter: SongPagingAdapter
    
    // 用于存储选中的封面图片
    private var selectedCoverUri: Uri? = null
    private var currentCoverPreview: ImageView? = null
    
    // 图片选择器
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedCoverUri = uri
                currentCoverPreview?.let { imageView ->
                    Glide.with(this)
                        .load(uri)
                        .centerCrop()
                        .into(imageView)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaylistBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 获取传递的歌单ID
        val playlistId = intent.getLongExtra("playlistId", -1L)
        if (playlistId != -1L) {
            viewModel.getPlaylistDetails(playlistId)
            viewModel.setPlaylistId(playlistId)
        } else {
            Toast.makeText(this, "歌单ID无效", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initView()
        initObserve()
    }

    private fun initView() {
        // 返回按钮
        binding.back.setOnClickListener {
            finish()
        }

        // 设置音乐列表适配器（分页）
        songAdapter = SongPagingAdapter { song ->
            onSongClick(song)
        }
        
        binding.songList.apply {
            layoutManager = LinearLayoutManager(this@PlaylistActivity)
            adapter = songAdapter
        }
        
        // 收藏按钮点击事件
        binding.collect.setOnClickListener {
            val playlistId = intent.getLongExtra("playlistId", -1L)
            if (playlistId != -1L) {
                viewModel.togglePlaylistCollection(playlistId)
            }
        }
        
        // 更多菜单按钮点击事件
        binding.moreMenus.setOnClickListener {
            val playlistId = intent.getLongExtra("playlistId", -1L)
            if (playlistId != -1L) {
                showPlaylistMenuDialog(playlistId)
            }
        }
    }

    private fun initObserve() {
        // 观察歌单详情
        lifecycleScope.launch {
            viewModel.playlistDetails.collectLatest { details ->
                details?.let {
                    updateUI(it)
                }
            }
        }
        
        // 观察收藏状态
        lifecycleScope.launch {
            viewModel.isCollected.collectLatest { isCollected ->
                updateCollectionUI(isCollected)
            }
        }

        // 观察加载状态
        lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                // 可以在这里显示/隐藏加载进度条
            }
        }

        // 观察错误信息
        lifecycleScope.launch {
            viewModel.errorMessage.collectLatest { error ->
                error?.let {
                    Toast.makeText(this@PlaylistActivity, it, Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        // 观察音乐列表（分页）
        lifecycleScope.launch {
            viewModel.playlistMusicFlow.collectLatest { pagingData ->
                songAdapter.submitData(pagingData)
            }
        }
    }
    
    /**
     * 更新收藏UI状态
     */
    private fun updateCollectionUI(isCollected: Boolean) {
        if (isCollected) {
            binding.collectionText.text = "已收藏"
            binding.collectionImage.setColorFilter(Color.parseColor("#fcb510"))
        } else {
            binding.collectionText.text = "收藏"
            binding.collectionImage.setColorFilter(Color.parseColor("#A0A0A0"))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(details: com.example.listen_to_the_clouds.data.model.PlaylistDetails) {
        // 设置歌单标题
        binding.title.text = details.playlistTitle

        // 设置用户名
        binding.userName.text = details.user

        // 设置歌曲数量
        binding.numberOfSongs.text = "${details.musicNumber} 首歌曲"

        // 设置播放量
        binding.playVolume.text = "${details.playlistTimes} 次播放"

        // 加载歌单封面
        Glide.with(this)
            .load(RESOURCE_ADDRESS + details.playlistCover)
            .placeholder(R.drawable.load)
            .error(R.drawable.load)
            .centerCrop()
            .into(binding.imageView4)

        // 加载用户头像
        Glide.with(this)
            .load(RESOURCE_ADDRESS + details.avatar)
            .placeholder(R.drawable.test)
            .error(R.drawable.test)
            .centerCrop()
            .into(binding.avatar)

        // 使用Palette提取封面颜色
        Glide.with(this)
            .asBitmap()
            .load(RESOURCE_ADDRESS + details.playlistCover)
            .into(object : com.bumptech.glide.request.target.CustomTarget<android.graphics.Bitmap>() {
                override fun onResourceReady(
                    resource: android.graphics.Bitmap,
                    transition: com.bumptech.glide.request.transition.Transition<in android.graphics.Bitmap>?
                ) {
                    Palette.from(resource).generate { palette ->
                        val vibrant = palette?.getVibrantColor(Color.GRAY) ?: Color.GRAY
                        val dominantColor = palette?.getDominantColor(Color.GRAY) ?: vibrant
                        binding.main.setBackgroundColor(dominantColor)
                        binding.title.setTextColor(dominantColor)
                    }
                }

                override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {}
            })
    }
    
    /**
     * 处理音乐点击事件
     */
    private fun onSongClick(song: com.example.listen_to_the_clouds.data.model.HomeSong) {
        // 获取当前已加载的歌曲列表
        val currentList = songAdapter.snapshot().items
        
        // 查找点击歌曲的索引
        val index = currentList.indexOfFirst { it.id == song.id }
        
        if (index != -1 && currentList.isNotEmpty()) {
            // 设置播放列表并播放指定歌曲
            MusicPlayerManager.setPlaylistAndPlay(currentList, index)
            Toast.makeText(this, "开始播放: ${song.name}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "播放失败，请稍后重试", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 显示歌单菜单底部弹窗
     */
    private fun showPlaylistMenuDialog(playlistId: Long) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_playlist_menu, null)
        bottomSheetDialog.setContentView(dialogView)
        
        // 编辑歌单选项
        val editPlaylistOption = dialogView.findViewById<com.google.android.material.card.MaterialCardView>(R.id.editPlaylistOption)
        editPlaylistOption.setOnClickListener {
            bottomSheetDialog.dismiss()
            showEditPlaylistDialog(playlistId)
        }
        
        // 删除歌单选项
        val deletePlaylistOption = dialogView.findViewById<com.google.android.material.card.MaterialCardView>(R.id.deletePlaylistOption)
        deletePlaylistOption.setOnClickListener {
            bottomSheetDialog.dismiss()
            showDeleteConfirmDialog(playlistId)
        }
        
        // 取消按钮
        val cancelButton = dialogView.findViewById<com.google.android.material.card.MaterialCardView>(R.id.cancelButton)
        cancelButton.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        
        bottomSheetDialog.show()
    }
    
    /**
     * 显示编辑歌单底部弹窗
     */
    private fun showEditPlaylistDialog(playlistId: Long) {
        // 重置选中的封面
        selectedCoverUri = null
        
        val bottomSheetDialog = BottomSheetDialog(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_playlist, null)
        bottomSheetDialog.setContentView(dialogView)
        
        // 获取输入框、按钮和封面预览
        val playlistNameInput = dialogView.findViewById<EditText>(R.id.playlistNameInput)
        val coverPreview = dialogView.findViewById<ImageView>(R.id.coverPreview)
        val selectCoverButton = dialogView.findViewById<com.google.android.material.card.MaterialCardView>(R.id.selectCoverButton)
        val saveButton = dialogView.findViewById<com.google.android.material.card.MaterialCardView>(R.id.saveButton)
        val cancelButton = dialogView.findViewById<com.google.android.material.card.MaterialCardView>(R.id.cancelButton)
        
        // 保存当前封面预览引用
        currentCoverPreview = coverPreview
        
        // 设置当前歌单信息
        viewModel.playlistDetails.value?.let { details ->
            playlistNameInput.setText(details.playlistTitle)
            
            // 加载当前封面
            Glide.with(this)
                .load(RESOURCE_ADDRESS + details.playlistCover)
                .placeholder(R.drawable.load)
                .error(R.drawable.test)
                .centerCrop()
                .into(coverPreview)
        }
        
        // 选择封面按钮点击事件
        selectCoverButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        }
        
        // 保存按钮点击事件
        saveButton.setOnClickListener {
            val newTitle = playlistNameInput.text.toString().trim()
            if (newTitle.isNotEmpty()) {
                // 如果选择了新封面，转换为 File
                val coverFile = selectedCoverUri?.let { uri ->
                    uriToFile(uri)
                }
                
                viewModel.updatePlaylist(playlistId, newTitle, coverFile) {
                    bottomSheetDialog.dismiss()
                }
            } else {
                Toast.makeText(this, "歌单名称不能为空", Toast.LENGTH_SHORT).show()
            }
        }
        
        // 取消按钮点击事件
        cancelButton.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        
        bottomSheetDialog.show()
    }
    
    /**
     * 将 Uri 转换为 File
     */
    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val file = File(cacheDir, "playlist_cover_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            
            file
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "图片处理失败: ${e.message}", Toast.LENGTH_SHORT).show()
            null
        }
    }
    
    /**
     * 显示删除确认对话框
     */
    private fun showDeleteConfirmDialog(playlistId: Long) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("删除歌单")
            .setMessage("确定要删除这个歌单吗？删除后无法恢复。")
            .setPositiveButton("删除") { dialog, _ ->
                viewModel.deletePlaylist(playlistId) {
                    // 删除成功后关闭当前页面
                    finish()
                }
                dialog.dismiss()
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}