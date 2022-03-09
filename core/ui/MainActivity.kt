package com.dev.podo.core.ui

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.Window
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dev.podo.R
import com.dev.podo.common.utils.visibility
import com.dev.podo.core.model.entities.ResultState
import com.dev.podo.core.viewmodel.MainActivityViewModel
import com.dev.podo.databinding.ActivityMainBinding
import com.dev.podo.podoplus.di.BillingApi
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarItemView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var billing: BillingApi

    private val binding: ActivityMainBinding by viewBinding()
    private val viewModel: MainActivityViewModel by viewModels()

    @ColorInt
    private var defaultStatusBarColor: Int? = null
    private var navView: BottomNavigationView? = null
    private var isTopLevelDestinationShown: Boolean = true
    private var isPodoNowFragment: Boolean = false

    override fun onResume() {
        super.onResume()
        changeTopMenuVisibility(isTopLevelDestinationShown)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        setContentView(R.layout.activity_main)
        configureBottomNavComponents()
        navView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_profile, R.id.navigation_home, R.id.navigation_manage_events
            )
        )

        viewModel.init(::updateNavView)

        setSupportActionBar(binding.mainToolbar)

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            val isTopLevelDestination =
                appBarConfiguration.topLevelDestinations.contains(destination.id)
            isTopLevelDestinationShown = isTopLevelDestination
            window.statusBarColor = ContextCompat.getColor(this, R.color.label_secondary)
            changeTopMenuVisibility(isTopLevelDestination)
            when (destination.id) {
                R.id.podoNowFragment -> {
                    // TODO: hideSystemUI()
                    defaultStatusBarColor = window.statusBarColor
                    updateNavigationBarColor(Color.BLACK)
                    updateStatusBarColor(Color.BLACK)
                    supportActionBar?.hide()
                    isPodoNowFragment = true
                }
                else -> {
                    isPodoNowFragment = false
                    updateNavigationBarColor(defaultStatusBarColor ?: Color.BLACK)
                }
            }
        }

        navView?.apply {
            setupActionBarWithNavController(navController, appBarConfiguration)
            val startItemId = R.id.navigation_home
            selectedItemId = startItemId
            itemIconTintList = null
            setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.navigation_home -> {
                        val logoIcon =
                            if (viewModel.user.hasSubscription) R.drawable.ic_home_vip_selected else R.drawable.ic_home_selected
                        navController.navigate(R.id.navigation_home)
                        it.setIcon(logoIcon)
                    }
                    R.id.navigation_profile -> {
                        val logoIcon =
                            if (viewModel.user.hasSubscription) R.drawable.ic_home_vip else R.drawable.ic_home
                        navController.navigate(R.id.navigation_profile)
                        menu.findItem(R.id.navigation_home).setIcon(logoIcon)
                    }
                    R.id.navigation_manage_events -> {
                        val logoIcon =
                            if (viewModel.user.hasSubscription) R.drawable.ic_home_vip else R.drawable.ic_home
                        navController.navigate(R.id.navigation_manage_events)
                        menu.findItem(R.id.navigation_home).setIcon(logoIcon)
                    }
                }
                return@setOnItemSelectedListener true
            }
        }

        lifecycleScope.launchWhenCreated {
            billing.startConnect(this@MainActivity).collect { result0 ->
                when (result0) {
                    is ResultState.Error -> {}
                    ResultState.InProgress -> {}
                    is ResultState.Success -> {
//                        billing.getPurchase().collect { result ->
//                            when (result) {
//                                is ResultState.Error -> {}
//                                ResultState.InProgress -> {}
//                                is ResultState.Success -> {
//                                    if (result.data.find { it.skus.contains("test.new.podo.plus") } != null) {
//                                        Storage.isPodoPlus = true
//                                        updateNavView()
//                                    }
//                                }
//                            }
//                        }
                    }
                }
            }
        }
    }

    private fun changeTopMenuVisibility(visible: Boolean) {
        navView?.visibility(visible)
        if (visible || isPodoNowFragment) {
            supportActionBar?.hide()
            return
        }
        supportActionBar?.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        val nav = findNavController(R.id.nav_host_fragment_activity_main)
        if (nav.previousBackStackEntry?.destination?.id == R.id.navigation_home) {
            binding.navView.selectedItemId = R.id.navigation_home
        } else {
            super.onBackPressed()
        }
    }

    fun updateActionBarTitle(newTitle: String) {
        supportActionBar?.title = newTitle
    }

    fun updateNavView() {
        binding.navView.apply {
            val item = menu.findItem(R.id.navigation_home)
            val logoIcon = if (!item.isChecked) {
                if (viewModel.user.hasSubscription) R.drawable.ic_home_vip else R.drawable.ic_home
            } else {
                if (viewModel.user.hasSubscription) R.drawable.ic_home_vip_selected else R.drawable.ic_home_selected
            }
            item.setIcon(logoIcon)
        }
    }

    private fun configureBottomNavComponents() {
        binding.navView.findViewById<NavigationBarItemView>(R.id.navigation_home)
            .transitionName = getString(R.string.splash_transaction_name)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    fun updateNavigationBarColor(@ColorInt color: Int) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = color
    }

    fun updateStatusBarColor(@ColorInt color: Int) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = color
        // resources.getColor(R.color.my_statusbar_color)
    }
}
