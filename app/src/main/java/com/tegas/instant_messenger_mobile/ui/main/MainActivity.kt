package com.tegas.instant_messenger_mobile.ui.main

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.tegas.instant_messenger_mobile.R
import com.tegas.instant_messenger_mobile.data.Result
import com.tegas.instant_messenger_mobile.data.retrofit.response.ChatsItem
import com.tegas.instant_messenger_mobile.databinding.ActivityMainSecondBinding
import com.tegas.instant_messenger_mobile.ui.ViewModelFactory
import com.tegas.instant_messenger_mobile.ui.detail.DetailActivity
import com.tegas.instant_messenger_mobile.ui.favorite.FavoriteActivity
import com.tegas.instant_messenger_mobile.ui.login.LoginActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainSecondBinding

    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private val adapter by lazy {
        ChatAdapter {
            Intent(this, DetailActivity::class.java).apply {
                putExtra("item", it)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(this)
            }
        }
    }

    companion object {
        @StringRes
        private val TAB_TITLES = intArrayOf(
            R.string.tab_text_1,
            R.string.tab_text_2,
            R.string.tab_text_3
        )
    }

    //    private val nim = "21106050048"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainSecondBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val nim = intent.getStringExtra("nim")

        getSession()
        setRecyclerView()
//        viewModel.getChatList(nim!!)
        fetchData()

        setLogout()

        setViewPager()

        binding.ivLines.setOnClickListener {
            val intent = Intent(this, FavoriteActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getSession() {
        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                startActivity(Intent(this, LoginActivity::class.java))
            }

            val nim = user.nim
            val name = user.name

            binding.tvName.text = name
            viewModel.getChatList(nim)
        }
    }

    private fun setRecyclerView() {
        binding.rvUser.layoutManager = LinearLayoutManager(this)
        binding.rvUser.setHasFixedSize(true)
        binding.rvUser.adapter = adapter
    }

    private fun fetchData() {
        viewModel.mainViewModel.observe(this) {
            when (it) {
                is Result.Loading -> {
                    Log.d("Result", "Loading")
                    binding.progressBar.visibility = View.VISIBLE
                }

                is Result.Error -> {
                    Log.d("Result", "Error")
                    binding.progressBar.visibility = View.GONE
                }

                is Result.Success -> {
                    Log.d("Result", "Success")
                    binding.progressBar.visibility = View.GONE
                    adapter.setData(it.data as MutableList<ChatsItem>)
                }
            }
        }
    }

    private fun setLogout() {
        binding.tvLogout.setOnClickListener {
            viewModel.logout()
        }
    }

    private fun setViewPager() {
        val sectionsPagerAdapter = SectionsPagerAdapter(this)
        val viewPager: ViewPager2 = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = resources.getString(TAB_TITLES[position])
        }.attach()

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.view?.background?.setColorFilter(
                    ContextCompat.getColor(this@MainActivity, R.color.blue),
                    PorterDuff.Mode.SRC_IN
                )

                tab?.view?.findViewById<TextView>(com.google.android.material.R.id.title)
                    ?.setTextColor(
                        ContextCompat.getColor(this@MainActivity, android.R.color.white)
                    )
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Reset background color when tab is unselected
                tab?.view?.background?.clearColorFilter()
                // Reset text color when tab is unselected
                tab?.view?.findViewById<TextView>(com.google.android.material.R.id.title)
                    ?.setTextColor(
                        ContextCompat.getColor(this@MainActivity, R.color.black)
                    )
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
}