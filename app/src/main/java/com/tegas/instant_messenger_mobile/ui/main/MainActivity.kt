package com.tegas.instant_messenger_mobile.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.tegas.instant_messenger_mobile.data.Result
import com.tegas.instant_messenger_mobile.data.retrofit.response.ChatsItem
import com.tegas.instant_messenger_mobile.databinding.ActivityMainSecondBinding
import com.tegas.instant_messenger_mobile.ui.ViewModelFactory
import com.tegas.instant_messenger_mobile.ui.detail.DetailActivity
import com.tegas.instant_messenger_mobile.ui.favorite.FavoriteActivity
import com.tegas.instant_messenger_mobile.ui.login.LoginActivity
import java.util.Locale
import androidx.appcompat.widget.SearchView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.ui.AppBarConfiguration
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.tegas.instant_messenger_mobile.R
import com.tegas.instant_messenger_mobile.databinding.ActivityMainBinding
import de.hdodenhof.circleimageview.CircleImageView


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<MainViewModel> { ViewModelFactory.getInstance(this) }
    private lateinit var nim: String
    private val mList = mutableListOf<ChatsItem>()
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var profileCircleImageView: CircleImageView
    private lateinit var tvName: TextView
    private lateinit var tvNim: TextView

    private val adapter by lazy {
        ChatAdapter {
            Intent(this, DetailActivity::class.java).apply {
                putExtra("item", it)
                putExtra("chatId", it.chatId)
                putExtra("chatName", it.name)
                putExtra("chatType", it.chatType)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(this)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.title = ""
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            binding.appBarMain.toolBar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        profileCircleImageView = navView.getHeaderView(0).findViewById(R.id.imageView)
        Glide.with(this)
            .load(R.drawable.daniela_villarreal)
            .into(profileCircleImageView)
        tvName  = navView.getHeaderView(0).findViewById(R.id.tv_name)
        tvNim  = navView.getHeaderView(0).findViewById(R.id.tv_nim)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_favorite, R.id.nav_logout
            ), drawerLayout
        )

        navView.setNavigationItemSelectedListener { menuItem ->
            when(menuItem.itemId) {
                R.id.nav_favorite -> {
                    val intent = Intent(this, FavoriteActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> {
                    viewModel.logout()
                    true
                }
            }
        }

        observeSession()
        setupRecyclerView()
        fetchData()
        setupLogout()
        setupSearch()
    }

    private fun observeSession() {
        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                startActivity(Intent(this, LoginActivity::class.java))
            } else {
                nim = user.nim
                tvName.text = user.name
                tvNim.text = nim
//                binding.tvName.text = user.name
                viewModel.getChatList(nim)
            }
        }
    }

    private fun setupRecyclerView() {
        binding.appBarMain.rvUser.layoutManager = LinearLayoutManager(this)
        binding.appBarMain.rvUser.setHasFixedSize(true)
        binding.appBarMain.rvUser.adapter = adapter
    }

    private fun fetchData() {
        viewModel.mainViewModel.observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    Log.d("Result", "Loading")
                    binding.appBarMain.progressBar.visibility = View.VISIBLE
                }

                is Result.Error -> {
                    Log.d("Result", "Error")
                    binding.appBarMain.progressBar.visibility = View.GONE
                }

                is Result.Success -> {
                    Log.d("Result", "Success")
                    binding.appBarMain.progressBar.visibility = View.GONE
                    mList.clear() // Clear existing list before update
                    mList.addAll(result.data)
                    adapter.setData(mList)
                }
            }
        }
    }

    private fun setupLogout() {
//        binding.tvLogout.setOnClickListener { viewModel.logout() }
    }

    private fun setupSearch() {
        binding.appBarMain.searchView.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterList(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })
    }

    private fun filterList(query: String?) {
        val filteredList = if (query.isNullOrEmpty()) {
            mList.toMutableList()
        } else {
            mList.filter { it.name.lowercase(Locale.ROOT).contains(query.lowercase(Locale.ROOT)) }
                .toMutableList()
        }

        if (filteredList.isEmpty()) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
        }

        adapter.setFilteredList(filteredList)
    }

    private fun onItemClick(item: ChatsItem) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("item", item)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.main, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.action_settings -> {
//                viewModel.logout()
//                true
//            }
//
//            else -> super.onOptionsItemSelected(item)
//        }
//    }
}
