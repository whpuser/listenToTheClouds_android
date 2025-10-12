package com.example.listen_to_the_clouds

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import com.example.listen_to_the_clouds.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val activeColor by lazy { ContextCompat.getColor(this, R.color.main_color) }
    private val inactiveColor by lazy { ContextCompat.getColor(this, R.color.nonSelective) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Fragment NavController
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // 初始状态
        setNavBarColors(selected = R.id.navigation_home)

        // 导航按钮点击事件
        binding.Home.setOnClickListener {
            if (navController.currentDestination?.id != R.id.navigation_home) {
                navController.navigate(R.id.navigation_home)
                setNavBarColors(R.id.navigation_home)
            }
        }

        binding.User.setOnClickListener {
            if (navController.currentDestination?.id != R.id.navigation_notifications) {
                navController.navigate(R.id.navigation_notifications)
                setNavBarColors(R.id.navigation_notifications)
            }
        }

        binding.fabCenter.setOnClickListener {
            if (navController.currentDestination?.id != R.id.navigation_dashboard) {
                navController.navigate(R.id.navigation_dashboard)
                setNavBarColors(R.id.navigation_dashboard)
            }
        }
    }

    // 设置底部导航栏颜色
    private fun setNavBarColors(selected: Int) {
        when (selected) {
            R.id.navigation_home -> {
                binding.btnHome.setColorFilter(activeColor)
                binding.btnHomeText.setTextColor(activeColor)
                binding.btnUser.setColorFilter(inactiveColor)
                binding.btnUserText.setTextColor(inactiveColor)
            }
            R.id.navigation_notifications -> {
                binding.btnHome.setColorFilter(inactiveColor)
                binding.btnHomeText.setTextColor(inactiveColor)
                binding.btnUser.setColorFilter(activeColor)
                binding.btnUserText.setTextColor(activeColor)
            }
            R.id.navigation_dashboard -> {
                binding.btnHome.setColorFilter(inactiveColor)
                binding.btnHomeText.setTextColor(inactiveColor)
                binding.btnUser.setColorFilter(inactiveColor)
                binding.btnUserText.setTextColor(inactiveColor)
            }
        }
    }
}
