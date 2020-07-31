package com.akvelon.weather

import android.os.Bundle
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var fragmentManager = supportFragmentManager
        var mainFragment = MainFragment()
        fragmentManager.beginTransaction().add(R.id.fragmentContainer, mainFragment).commit()
    }
}
