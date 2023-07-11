package com.example.mystorysubmission.ui.main

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mystorysubmission.R
import com.example.mystorysubmission.adapter.LoadingStateAdapter
import com.example.mystorysubmission.adapter.StoryAdapter
import com.example.mystorysubmission.databinding.ActivityMainBinding
import com.example.mystorysubmission.helper.ViewModelFactory
import com.example.mystorysubmission.ui.add.AddActivity
import com.example.mystorysubmission.ui.login.LoginActivity
import com.example.mystorysubmission.ui.maps.MapsActivity
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user")

class MainActivity : AppCompatActivity(), View.OnClickListener, Toolbar.OnMenuItemClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var factory: ViewModelFactory
    private val mainViewModel: MainViewModel by viewModels { factory }
    private lateinit var storyAdapter: StoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupView()

        factory = ViewModelFactory.getInstance(this)

        binding.rvStories.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            storyAdapter = StoryAdapter()
            adapter = storyAdapter.withLoadStateFooter(
                footer = LoadingStateAdapter { storyAdapter.retry() }
            )
        }

        setupData()

        mainViewModel.getUser().observe(this) { user ->
            val token = user?.token
            if (!token.isNullOrEmpty()) {
                mainKey(token)
            } else {
                val i = Intent(this, LoginActivity::class.java)
                startActivity(i)
                finish()
            }
        }

        mainViewModel.errorMessage.observe(this) {
            if (it != "") {
                AlertDialog.Builder(this).apply {
                    setTitle(R.string.data_error)
                    setMessage(R.string.data_error_message.toString() + " $it")
                    setPositiveButton(R.string.ok) { _, _ -> }
                    create()
                    show()
                }
            }
        }

        mainViewModel.loading.observe(this) {
            showLoading(it)
        }

        binding.fabAdd.setOnClickListener(this)

    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setOnMenuItemClickListener(this)
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.title = resources.getString(R.string.app_name)
    }


    private fun mainKey(token: String) {
        if (token.isEmpty()) {
            val i = Intent(this, LoginActivity::class.java)
            startActivity(i)
            finish()
        } else {
            setupData()
        }
    }

    private fun setupData() {
        mainViewModel.storyPaging().observe(this@MainActivity) { pagingData ->
            lifecycleScope.launch {
                storyAdapter.submitData(pagingData)
            }
        }
        binding.rvStories.layoutManager = LinearLayoutManager(this@MainActivity)
        storyAdapter = StoryAdapter()
        binding.rvStories.adapter = storyAdapter.withLoadStateFooter(
            footer = LoadingStateAdapter {
                storyAdapter.retry()
            }
        )
    }


    private fun showLoading(isLoading: Boolean) {
        with(binding) {
            if (isLoading) {
                pbLoading.visibility = View.VISIBLE
                bgLoading.visibility = View.VISIBLE
            } else {
                pbLoading.visibility = View.INVISIBLE
                bgLoading.visibility = View.INVISIBLE
            }
        }
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.fab_add) startActivity(Intent(this, AddActivity::class.java))
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.app_bar_setting -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
                return true
            }

            R.id.app_bar_logout -> {
                AlertDialog.Builder(this).apply {
                    setTitle(R.string.logout)
                    setMessage(R.string.logout_confirmation)
                    setPositiveButton(R.string.yes) { _, _ ->
                        mainViewModel.logout()

                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                        finish()
                    }
                    setNegativeButton(R.string.cancel) { _, _ ->

                    }
                    create()
                    show()

                    return true
                }
            }

            R.id.app_bar_map -> {
                val intent = Intent(this@MainActivity, MapsActivity::class.java)
                startActivity(intent)

            }

            else -> return false
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        return true
    }

}