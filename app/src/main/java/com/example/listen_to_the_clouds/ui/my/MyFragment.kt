package com.example.listen_to_the_clouds.ui.my

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.listen_to_the_clouds.MyApplication
import com.example.listen_to_the_clouds.R
import com.example.listen_to_the_clouds.data.network.RESOURCE_ADDRESS
import com.example.listen_to_the_clouds.databinding.FragmentMyBinding
import com.example.listen_to_the_clouds.ui.activity.login.LoginActivity
import com.example.listen_to_the_clouds.ui.activity.favorites.FavoritesListActivity
import com.example.listen_to_the_clouds.ui.activity.profile.UserProfileActivity
import com.example.listen_to_the_clouds.utils.TokenManager

class MyFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentMyBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MyViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        )[MyViewModel::class.java]
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
        
        // 顶部统计数字点击事件
        binding.favoritePlaylist.setOnClickListener(this)  // 收藏歌曲数量
        binding.favoriteSongs.setOnClickListener(this)     // 收藏歌单数量
        binding.createAPlaylist.setOnClickListener(this)   // 创建歌单数量
    }

    @SuppressLint("SetTextI18n")
    private fun initObserve() {
        val token = TokenManager.getToken(MyApplication.getInstance())
        Log.d("TAG", "token: $token")
        // 观察用户信息
        viewModel.userInfo.observe(viewLifecycleOwner) { userInfo ->
            binding.username.text = userInfo?.name//用户名
            binding.UserAccount.text = userInfo?.account//账号
            //用户头像
            Glide.with(this)
                .load(RESOURCE_ADDRESS + userInfo?.avatar)
                .placeholder(R.drawable.load)
                .error(R.drawable.log)
                .into(binding.avatar)
            Log.d("MyFragment", "观察用户信息: $userInfo")
        }

        // 更新用户收藏与创建总数
        viewModel.myTotal.observe(viewLifecycleOwner) {
                binding.favoritePlaylist.text = it?.collectMusic.toString()
                binding.favoriteSongs.text = it?.collectPlaylists.toString()
                binding.createAPlaylist.text = it?.createPlaylists.toString()

        }

        // 观察创建歌单结果
        viewModel.createPlaylistResult.observe(viewLifecycleOwner) { result ->
            Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
        }

        // 观察用户反馈结果
        viewModel.feedbackResult.observe(viewLifecycleOwner) { result ->
            Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
        }

        Log.d("TAG", "登录状态: ${ viewModel.isLoggedIn}---${ if (!TokenManager.isUserLoggedIn()) "用户已经登录" else "用户未登录" }")
    }


    override fun onClick(v: View?) {
        when (v) {
            binding.logIn -> {
                context?.let {
                    if (!TokenManager.isLoggedIn(it)) {
                        // 未登录，跳转到登录页面
                        val intent = Intent(requireContext(), LoginActivity::class.java)
                        startActivity(intent)
                    } else {
                        // 已登录，跳转到用户详情页面
                        val intent = Intent(requireContext(), UserProfileActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
            // 收藏歌曲
            binding.collectMusic -> {
                if (checkLoginAndNavigate()) {
                    val intent = Intent(requireContext(), FavoritesListActivity::class.java)
                    intent.putExtra(FavoritesListActivity.EXTRA_LIST_TYPE, FavoritesListActivity.TYPE_FAVORITE_SONGS)
                    startActivity(intent)
                }
            }
            // 收藏歌单
            binding.collectPlatList -> {
                if (checkLoginAndNavigate()) {
                    val intent = Intent(requireContext(), FavoritesListActivity::class.java)
                    intent.putExtra(FavoritesListActivity.EXTRA_LIST_TYPE, FavoritesListActivity.TYPE_FAVORITE_PLAYLISTS)
                    startActivity(intent)
                }
            }
            // 创建歌单（点击显示对话框）
            binding.createPlatList -> {
                showCreatePlaylistDialog()
            }
            // 点击收藏歌曲数量，跳转到收藏歌曲列表
            binding.favoritePlaylist -> {
                if (checkLoginAndNavigate()) {
                    val intent = Intent(requireContext(), FavoritesListActivity::class.java)
                    intent.putExtra(FavoritesListActivity.EXTRA_LIST_TYPE, FavoritesListActivity.TYPE_FAVORITE_SONGS)
                    startActivity(intent)
                }
            }
            // 点击收藏歌单数量，跳转到收藏歌单列表
            binding.favoriteSongs -> {
                if (checkLoginAndNavigate()) {
                    val intent = Intent(requireContext(), FavoritesListActivity::class.java)
                    intent.putExtra(FavoritesListActivity.EXTRA_LIST_TYPE, FavoritesListActivity.TYPE_FAVORITE_PLAYLISTS)
                    startActivity(intent)
                }
            }
            // 点击创建歌单数量，跳转到创建的歌单列表
            binding.createAPlaylist -> {
                if (checkLoginAndNavigate()) {
                    val intent = Intent(requireContext(), FavoritesListActivity::class.java)
                    intent.putExtra(FavoritesListActivity.EXTRA_LIST_TYPE, FavoritesListActivity.TYPE_CREATED_PLAYLISTS)
                    startActivity(intent)
                }
            }
            // 用户歌单（原本地上传）
            binding.userFeedback -> {
                if (checkLoginAndNavigate()) {
                    val intent = Intent(requireContext(), FavoritesListActivity::class.java)
                    intent.putExtra(FavoritesListActivity.EXTRA_LIST_TYPE, FavoritesListActivity.TYPE_CREATED_PLAYLISTS)
                    startActivity(intent)
                }
            }
            // 用户反馈
            binding.uploadAudio -> {
                showFeedbackDialog()
            }
        }
    }

    /**
     * 检查登录状态并导航
     */
    private fun checkLoginAndNavigate(): Boolean {
        context?.let {
            if (!TokenManager.isLoggedIn(it)) {
                Toast.makeText(requireContext(), "请先登录", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }

    /**
     * 显示创建歌单对话框（底部弹出）
     */
    private fun showCreatePlaylistDialog() {
        // 检查登录状态
        context?.let {
            if (!TokenManager.isLoggedIn(it)) {
                Toast.makeText(requireContext(), "请先登录", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // 创建底部弹窗
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_playlist, null)
        bottomSheetDialog.setContentView(dialogView)

        // 获取输入框和按钮
        val playlistNameInput = dialogView.findViewById<EditText>(R.id.playlistNameInput)
        val createButton = dialogView.findViewById<View>(R.id.createButton)
        val cancelButton = dialogView.findViewById<View>(R.id.cancelButton)

        // 创建按钮点击事件
        createButton.setOnClickListener {
            val title = playlistNameInput.text.toString().trim()
            viewModel.createPlaylist(title)
            bottomSheetDialog.dismiss()
        }

        // 取消按钮点击事件
        cancelButton.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    /**
     * 显示用户反馈对话框（底部弹出）
     */
    private fun showFeedbackDialog() {
        // 检查登录状态
        context?.let {
            if (!TokenManager.isLoggedIn(it)) {
                Toast.makeText(requireContext(), "请先登录", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // 创建底部弹窗
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_user_feedback, null)
        bottomSheetDialog.setContentView(dialogView)

        // 获取输入框和按钮
        val feedbackInput = dialogView.findViewById<EditText>(R.id.feedbackInput)
        val submitButton = dialogView.findViewById<View>(R.id.submitButton)
        val cancelButton = dialogView.findViewById<View>(R.id.cancelButton)

        // 提交按钮点击事件
        submitButton.setOnClickListener {
            val content = feedbackInput.text.toString().trim()
            viewModel.submitFeedback(content)
            bottomSheetDialog.dismiss()
        }

        // 取消按钮点击事件
        cancelButton.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshUserData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}