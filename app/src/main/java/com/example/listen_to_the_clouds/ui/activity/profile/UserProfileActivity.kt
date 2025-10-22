package com.example.listen_to_the_clouds.ui.activity.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.listen_to_the_clouds.R
import com.example.listen_to_the_clouds.data.network.RESOURCE_ADDRESS
import com.example.listen_to_the_clouds.databinding.ActivityUserProfileBinding
import com.example.listen_to_the_clouds.ui.activity.login.LoginActivity
import com.example.listen_to_the_clouds.utils.TokenManager

class UserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserProfileBinding
    private lateinit var viewModel: UserProfileViewModel
    private var selectedImageUri: Uri? = null

    // 图片选择器
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                binding.avatarImage.setImageURI(uri)
                // 上传头像
                viewModel.uploadAvatar(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[UserProfileViewModel::class.java]

        initView()
        initObserve()
        loadUserInfo()
    }

    private fun initView() {
        // 返回按钮
        binding.backButton.setOnClickListener {
            finish()
        }

        // 头像点击 - 选择图片
        binding.avatarCard.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        }

        // 保存按钮
        binding.saveButton.setOnClickListener {
            saveUserInfo()
        }

        // 退出登录按钮
        binding.logoutButton.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun initObserve() {
        // 观察用户信息
        viewModel.userInfo.observe(this) { userInfo ->
            userInfo?.let {
                binding.nameInput.setText(it.name)
                binding.ageInput.setText(it.age.toString())
                
                // 设置性别
                when (it.gender) {
                    0 -> binding.maleRadio.isChecked = true
                    1 -> binding.femaleRadio.isChecked = true
                }

                // 加载头像
                Glide.with(this)
                    .load(RESOURCE_ADDRESS + it.avatar)
                    .placeholder(R.drawable.log)
                    .error(R.drawable.log)
                    .into(binding.avatarImage)
            }
        }

        // 观察更新结果
        viewModel.updateResult.observe(this) { result ->
            Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
            if (result.success) {
                // 更新成功后刷新数据
                loadUserInfo()
            }
        }

        // 观察上传头像结果
        viewModel.uploadAvatarResult.observe(this) { result ->
            Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
            if (result.success) {
                // 上传成功后刷新用户信息
                loadUserInfo()
            }
        }
    }

    private fun loadUserInfo() {
        viewModel.loadUserInfo()
    }

    private fun saveUserInfo() {
        val name = binding.nameInput.text.toString().trim()
        val ageStr = binding.ageInput.text.toString().trim()
        val gender = when {
            binding.maleRadio.isChecked -> 0
            binding.femaleRadio.isChecked -> 1
            else -> 0
        }

        if (name.isEmpty()) {
            Toast.makeText(this, "请输入用户名", Toast.LENGTH_SHORT).show()
            return
        }

        if (ageStr.isEmpty()) {
            Toast.makeText(this, "请输入年龄", Toast.LENGTH_SHORT).show()
            return
        }

        val age = ageStr.toIntOrNull() ?: 0
        if (age <= 0 || age > 150) {
            Toast.makeText(this, "请输入有效的年龄", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.updateUserInfo(name, gender, age)
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("退出登录")
            .setMessage("确定要退出登录吗？")
            .setPositiveButton("确定") { dialog, _ ->
                logout()
                dialog.dismiss()
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun logout() {
        // 清除Token
        TokenManager.clearToken(this)
        
        Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show()
        
        // 跳转到登录页面
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
