package com.tegas.instant_messenger_mobile.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
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


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainSecondBinding

    private var mList = mutableListOf<ChatsItem>()

    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private lateinit var nim: String

    private val adapter by lazy {
        ChatAdapter {
            Intent(this, DetailActivity::class.java).apply {
                putExtra("item", it)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(this)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainSecondBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getSession()
        setRecyclerView()
        fetchData()

        setLogout()


        binding.ivLines.setOnClickListener {
            val intent = Intent(this, FavoriteActivity::class.java)
            startActivity(intent)
        }

        binding.searchView.setOnQueryTextListener(object :  SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterList(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true  // Return true to indicate that the query has been handled
            }

        })
    }

    private fun filterList(query: String?) {
        val filteredList = if (query.isNullOrBlank()) {
            // Show original list when query is null or blank
            mList.toMutableList()
        } else {
            // Filter list based on query
            mList.filter { it.name.lowercase(Locale.ROOT).contains(query.lowercase(Locale.ROOT)) }.toMutableList()
        }

        if (filteredList.isEmpty()) {
            Toast.makeText(applicationContext, "User not found", Toast.LENGTH_SHORT).show()
        }

        adapter.setFilteredList(filteredList)
    }

    private fun getSession() {
        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                startActivity(Intent(this, LoginActivity::class.java))
            } else {
            nim = user.nim
            val name = user.name

            binding.tvName.text = name
            viewModel.getChatList(nim)
                }
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
                    mList = it.data as MutableList<ChatsItem>
                    adapter.setData(mList)
                }
            }
        }
    }

    private fun setLogout() {
        binding.tvLogout.setOnClickListener {
            viewModel.logout()
        }
    }
}