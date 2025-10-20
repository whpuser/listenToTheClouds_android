package com.example.listen_to_the_clouds

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.fragment.NavHostFragment
import com.example.listen_to_the_clouds.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private val activeColor by lazy { ContextCompat.getColor(this, R.color.main_color) }
    private val inactiveColor by lazy { ContextCompat.getColor(this, R.color.nonSelective) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 状态栏透明 - 告诉系统不要预留状态栏空间
        window.statusBarColor = Color.TRANSPARENT
        WindowCompat.setDecorFitsSystemWindows(window, false)

        initView()
        initObserve()

    }

    private fun initView() {
        // 初始化导航控制器
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // 初始化时根据当前目的地同步底部导航栏状态
        val initialId = navController.currentDestination?.id ?: R.id.navigation_home
        setNavBarColors(initialId)

        // 目的地变化时同步底部导航栏状态（适配从其他页面/逻辑触发的跳转，含子页面）
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when {
                destination.hierarchy.any { it.id == R.id.navigation_home } ->
                    setNavBarColors(R.id.navigation_home)
                destination.hierarchy.any { it.id == R.id.navigation_notifications } ->
                    setNavBarColors(R.id.navigation_notifications)
                destination.hierarchy.any { it.id == R.id.navigation_dashboard } ->
                    setNavBarColors(R.id.navigation_dashboard)
            }
        }

        //导航切换逻辑
        binding.Home.setOnClickListener {
            navigateSafe(R.id.navigation_home)
        }
        binding.User.setOnClickListener {
            navigateSafe(R.id.navigation_notifications)
        }
        binding.fabCenter.setOnClickListener {
            navigateSafe(R.id.navigation_dashboard)
        }
    }

    private fun initObserve() {

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

    //切换时页面销毁与创建
    private fun navigateSafe(destinationId: Int) {
        // 已经在同一分区（含子页面）则不执行任何行为
        val isSameSection = navController.currentDestination?.hierarchy?.any { it.id == destinationId } == true
        if (isSameSection) return
        val navOptions = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setPopUpTo(navController.graph.startDestinationId, inclusive = false) // 保留首页
            .build()
        navController.navigate(destinationId, null, navOptions)
    }
}
