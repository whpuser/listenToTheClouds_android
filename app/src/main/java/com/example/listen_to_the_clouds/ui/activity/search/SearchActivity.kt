package com.example.listen_to_the_clouds.ui.activity.search

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.listen_to_the_clouds.databinding.ActivitySearchBinding
import com.example.listen_to_the_clouds.ui.search.MusicFragment
import com.example.listen_to_the_clouds.ui.search.PlaylistFragment
import com.google.android.material.tabs.TabLayoutMediator

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private val viewModel: SearchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initView()
        initObserve()
    }

    private fun initView() {
        // 设置 ViewPager2
        setupViewPager()
        
        // 设置搜索框
        setupSearchBar()
        
        // 返回按钮
        binding.searchBar.back.setOnClickListener {
            finish()
        }
    }

    private fun setupViewPager() {
        val fragments = listOf(
            MusicFragment(),
            PlaylistFragment()
        )
        
        val titles = listOf("歌曲", "歌单")
        
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = fragments.size
            override fun createFragment(position: Int): Fragment = fragments[position]
        }
        
        // 关联 TabLayout 和 ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()
    }

    private fun setupSearchBar() {
        // 搜索按钮点击事件
        binding.searchBar.confirmSearch.setOnClickListener {
            performSearch()
        }
        
        // 搜索框回车事件
        binding.searchBar.searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }
        
//         实时搜索（可选，如果不需要可以删除）
//         binding.searchBar.searchInput.addTextChangedListener {
//             val keyword = it.toString().trim()
//             if (keyword.isNotEmpty()) {
//                 viewModel.search(keyword)
//             } else {
//                 viewModel.clearResults()
//             }
//         }
    }

    private fun performSearch() {
        val keyword = binding.searchBar.searchInput.text.toString().trim()
        if (keyword.isNotEmpty()) {
            viewModel.search(keyword)
            // 隐藏键盘
            binding.searchBar.searchInput.clearFocus()
        }
    }

    private fun initObserve() {
        // 观察错误信息
        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                // 可以在这里显示 Toast 或 Snackbar
                // Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }
}