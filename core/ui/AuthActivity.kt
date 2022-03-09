package com.dev.podo.core.ui

import android.os.Bundle
import android.transition.ChangeBounds
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dev.podo.R
import com.dev.podo.core.viewmodel.AuthViewModel
import com.dev.podo.databinding.ActivityAuthBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : AppCompatActivity(R.layout.activity_auth) {

    lateinit var navController: NavController
    private val binding: ActivityAuthBinding by viewBinding()
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTransactionDuration()
        setNavController()
        configureActionBar()
    }

    private fun setTransactionDuration() {
        val bounds = ChangeBounds()
        bounds.duration = 600
        window.sharedElementEnterTransition = bounds
    }

    private fun configureActionBar() {
        setSupportActionBar(binding.authToolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowHomeEnabled(false)
            setDisplayShowTitleEnabled(false)
        }
    }

    private fun setNavController() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_auth) as NavHostFragment
        navController = navHostFragment.navController
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun changeLogoVisibility(visibility: Int) {
        binding.authToolbarLogo.visibility = visibility
    }

    fun changeActionBarTitle(newTitle: String = "") {
        binding.authToolbar.title = newTitle
    }
}
