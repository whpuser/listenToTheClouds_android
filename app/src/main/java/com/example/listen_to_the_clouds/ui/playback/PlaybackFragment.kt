package com.example.listen_to_the_clouds.ui.playback

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import com.google.android.material.slider.Slider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope

import com.bumptech.glide.Glide
import com.example.listen_to_the_clouds.R
import com.example.listen_to_the_clouds.databinding.FragmentDashboardBinding
import com.example.listen_to_the_clouds.utils.ImageBlurUtils
import com.example.listen_to_the_clouds.player.PlayMode
import com.example.listen_to_the_clouds.data.network.RESOURCE_ADDRESS
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PlaybackFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: PlaybackViewModel
    
    // 进度更新 Handler
    private val handler = Handler(Looper.getMainLooper())
    private val updateProgressRunnable = object : Runnable {
        override fun run() {
            viewModel.updateProgress()
            handler.postDelayed(this, 1000) // 每秒更新一次
        }
    }
    
    // 封面旋转动画
    private var rotationAnimator: ObjectAnimator? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[PlaybackViewModel::class.java]
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        initView()
        initObserve()
        
        // 如果没有播放歌曲，加载默认歌曲列表
        viewModel.loadDefaultSongIfNeeded()

        return binding.root
    }

    private fun initView() {
        //背景封面
        val imageView = binding.frontCover
        imageView.scaleX = 1.5f  // 放大 150%
        imageView.scaleY = 1.5f

        //点击事件
        binding.random.setOnClickListener(this)             //单曲循环/随机播放
        binding.previousSong.setOnClickListener(this)       //上一曲
        binding.playingStatus.setOnClickListener(this)      //播放状态
        binding.nextSong.setOnClickListener(this)           //下一曲
        binding.collect.setOnClickListener(this)            //收藏状态

        //进度条拖动监听
        binding.musicSlider.addOnChangeListener { slider, value, fromUser ->
            if (fromUser) {
                viewModel.seekTo(value.toInt())
            }
        }
        
        // 开始更新进度
        handler.post(updateProgressRunnable)
    }

    private fun initObserve() {
        // 观察当前播放歌曲
        lifecycleScope.launch {
            viewModel.currentSong.collectLatest { song ->
                song?.let {
                    updateSongUI(it)
                }
            }
        }

        // 观察播放状态
        lifecycleScope.launch {
            viewModel.isPlaying.collectLatest { isPlaying ->
                updatePlayPauseButton(isPlaying)
            }
        }

        // 观察播放模式
        lifecycleScope.launch {
            viewModel.playMode.collectLatest { mode ->
                updatePlayModeButton(mode)
            }
        }

        // 观察播放进度
        lifecycleScope.launch {
            viewModel.currentPosition.collectLatest { position ->
                // 只有在有效范围内才更新进度
                if (position >= 0 && binding.musicSlider.valueTo > 0) {
                    binding.musicSlider.value = position.toFloat().coerceIn(0f, binding.musicSlider.valueTo)
                }
                binding.min.text = formatTime(position)
            }
        }

        // 观察歌曲总时长
        lifecycleScope.launch {
            viewModel.duration.collectLatest { duration ->
                // 确保 valueTo 大于 valueFrom (0)
                if (duration > 0) {
                    // 先重置 value 为 0，避免切换歌曲时 value > valueTo 导致崩溃
                    binding.musicSlider.value = 0f
                    binding.musicSlider.valueTo = duration.toFloat()
                    binding.max.text = formatTime(duration)
                } else {
                    // 设置默认值避免崩溃
                    binding.musicSlider.value = 0f
                    binding.musicSlider.valueTo = 100f
                    binding.max.text = "00:00"
                }
            }
        }
    }

    /**
     * 更新歌曲UI
     */
    private fun updateSongUI(song: com.example.listen_to_the_clouds.data.model.HomeSong) {
        // 更新歌曲名称
        binding.title.text = song.name
        // 更新歌手名称
//        binding.artistName.text = song.artist
        
        // 加载封面图片
        Glide.with(this)
            .load(RESOURCE_ADDRESS + song.cover)
            .placeholder(R.drawable.load)
            .error(R.drawable.load)
            .into(binding.songListCover)
        
        // 加载并模糊背景
        Glide.with(this)
            .asBitmap()
            .load(RESOURCE_ADDRESS + song.cover)
            .placeholder(R.drawable.test)
            .error(R.drawable.test)
            .into(object : com.bumptech.glide.request.target.CustomTarget<android.graphics.Bitmap>() {
                override fun onResourceReady(
                    resource: android.graphics.Bitmap,
                    transition: com.bumptech.glide.request.transition.Transition<in android.graphics.Bitmap>?
                ) {
                    ImageBlurUtils.blurInto(binding.frontCover, resource, requireContext(), blurSize = 5.0f)
                }

                override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {}
            })
    }

    /**
     * 更新播放/暂停按钮
     */
    private fun updatePlayPauseButton(isPlaying: Boolean) {
        if (isPlaying) {
            binding.playingStatus.setImageResource(R.drawable.pause) // 播放中显示暂停图标
            startRotationAnimation()
        } else {
            binding.playingStatus.setImageResource(R.drawable.start) // 暂停中显示播放图标
            pauseRotationAnimation()
        }
    }
    
    /**
     * 开始旋转动画
     */
    private fun startRotationAnimation() {
        if (rotationAnimator == null) {
            rotationAnimator = ObjectAnimator.ofFloat(binding.songListCover, "rotation", 0f, 360f).apply {
                duration = 20000 // 20秒旋转一圈
                interpolator = LinearInterpolator()
                repeatCount = ObjectAnimator.INFINITE
                repeatMode = ObjectAnimator.RESTART
            }
        }
        
        if (rotationAnimator?.isPaused == true) {
            rotationAnimator?.resume()
        } else if (rotationAnimator?.isRunning != true) {
            rotationAnimator?.start()
        }
    }
    
    /**
     * 暂停旋转动画
     */
    private fun pauseRotationAnimation() {
        rotationAnimator?.pause()
    }
    
    /**
     * 停止旋转动画
     */
    private fun stopRotationAnimation() {
        rotationAnimator?.cancel()
        rotationAnimator = null
        binding.songListCover.rotation = 0f
    }

    /**
     * 更新播放模式按钮
     */
    private fun updatePlayModeButton(mode: PlayMode) {
        when (mode) {
            PlayMode.LIST_LOOP -> {
                binding.random.setImageResource(R.drawable.cycle) // 列表循环图标
            }
            PlayMode.SINGLE_LOOP -> {
                binding.random.setImageResource(R.drawable.single_loop) // 单曲循环图标
            }
            PlayMode.RANDOM -> {
                binding.random.setImageResource(R.drawable.random) // 随机播放图标
            }
        }
    }

    /**
     * 格式化时间（毫秒转 mm:ss）
     */
    private fun formatTime(milliseconds: Int): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    override fun onClick(v: View?) {
        when (v) {
            //切换播放模式（列表循环/单曲循环/随机播放）
            binding.random -> {
                viewModel.togglePlayMode()
            }
            //上一曲
            binding.previousSong -> {
                viewModel.playPrevious()
            }
            //播放/暂停
            binding.playingStatus -> {
                viewModel.togglePlayPause()
            }
            //下一曲
            binding.nextSong -> {
                viewModel.playNext()
            }
            //收藏状态
            binding.collect -> {
                // TODO: 实现收藏功能
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 停止进度更新
        handler.removeCallbacks(updateProgressRunnable)
        // 停止旋转动画
        stopRotationAnimation()
        _binding = null
    }
}