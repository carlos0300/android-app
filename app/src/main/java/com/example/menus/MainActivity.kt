package com.example.menus


import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.menus.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.FirebaseDatabase


class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding

    private lateinit var drawerLayout: DrawerLayout

    private lateinit var toggle: ActionBarDrawerToggle

    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseDatabase.setPersistenceEnabled(true)

        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        drawerLayout = mBinding.drawerLayout

        var toolbar = mBinding.toolbar

        setSupportActionBar(toolbar)

        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        replaceFragment(ProductsFragment())

        navigationView = mBinding.navView
        navigationView.bringToFront()
        navigationView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_products -> {
                    replaceFragment(ProductsFragment())
                    true
                }
                R.id.nav_daily_reports -> {
                    replaceFragment(DailyReportsFragment())
                    true
                }
                R.id.nav_profile -> {
                    replaceFragment(ProfileFragment())
                    true

                }
                R.id.nav_settings -> {
                    replaceFragment(SettingsFragment())
                    true
                }
                R.id.nav_about -> {
                    replaceFragment(AboutFragment())
                    true
                }
                R.id.nav_logout -> {
                    true
                }
                else -> {

                    false
                }
            }

        }

    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()

        // Cierra la ventana NavView aplicando un delay de 200ms
        drawerLayout.postDelayed({
            drawerLayout.closeDrawer(GravityCompat.START)
        }, 200)
    }


}
