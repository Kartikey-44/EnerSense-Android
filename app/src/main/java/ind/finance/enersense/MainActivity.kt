package ind.finance.enersense

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val topAppBar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.topAppBar)
        val navHostFragmentView = findViewById<androidx.fragment.app.FragmentContainerView>(R.id.nav_host_fragment)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { _, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())

            // Pad toolbar so title/avatar sit below status bar (time/battery)
            topAppBar.setPadding(0, statusBars.top, 0, 0)

            // Pad bottom navigation so icons/labels sit above gesture bar / 3-button bar
            bottomNav.setPadding(0, 0, 0, navBars.bottom)

            // Measure or compute effective bottom bar height (64dp standard + system nav bar)
            val density = resources.displayMetrics.density
            val defaultTopBarPx = (56 * density).toInt() + statusBars.top
            val defaultBottomNavPx = (64 * density).toInt() + navBars.bottom

            val params = navHostFragmentView.layoutParams as androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams
            params.topMargin = defaultTopBarPx
            params.bottomMargin = defaultBottomNavPx
            navHostFragmentView.layoutParams = params

            insets
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.destination_home -> bottomNav.menu.findItem(R.id.destination_home)?.isChecked = true
                R.id.destination_analytics -> bottomNav.menu.findItem(R.id.destination_analytics)?.isChecked = true
                R.id.destination_history -> bottomNav.menu.findItem(R.id.destination_history)?.isChecked = true
                R.id.destination_forecast -> bottomNav.menu.findItem(R.id.destination_forecast)?.isChecked = true
                R.id.destination_profile -> bottomNav.menu.findItem(R.id.destination_profile)?.isChecked = true
            }
        }

        findViewById<android.widget.ImageView>(R.id.ivProfileHeader)?.setOnClickListener {
            if (navController.currentDestination?.id != R.id.destination_profile) {
                val navOptions = androidx.navigation.NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .setRestoreState(true)
                    .setPopUpTo(
                        navController.graph.startDestinationId,
                        inclusive = false,
                        saveState = true
                    )
                    .build()
                navController.navigate(R.id.destination_profile, null, navOptions)
            }
        }
    }
}
