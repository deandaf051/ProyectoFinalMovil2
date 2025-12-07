package com.example.proyectofinalmovil3

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2

class ActivityHome : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        supportActionBar?.hide()

        viewPager = findViewById(R.id.viewPagerHome)
        viewPager.adapter = HomePagerAdapter(this)

        setupBottomNav()
        syncBottomNavWithPager()
    }

    private fun setupBottomNav() {
        val navInicio = findViewById<LinearLayout>(R.id.navInicio)
        val navMapa = findViewById<LinearLayout>(R.id.navMapa)
        val navEventos = findViewById<LinearLayout>(R.id.navEventos)
        val navStats = findViewById<LinearLayout>(R.id.navStats)
        val navPerfil = findViewById<LinearLayout>(R.id.navPerfil)

        navInicio.setOnClickListener { viewPager.currentItem = 0 }
        navMapa.setOnClickListener { viewPager.currentItem = 1 }
        navEventos.setOnClickListener { viewPager.currentItem = 2 }
        navStats.setOnClickListener { viewPager.currentItem = 3 }
        navPerfil.setOnClickListener { viewPager.currentItem = 4 }
    }

    private fun syncBottomNavWithPager() {
        val navs = listOf(
            findViewById<LinearLayout>(R.id.navInicio),
            findViewById<LinearLayout>(R.id.navMapa),
            findViewById<LinearLayout>(R.id.navEventos),
            findViewById<LinearLayout>(R.id.navStats),
            findViewById<LinearLayout>(R.id.navPerfil)
        )

        val colorSelected = ContextCompat.getColor(this, R.color.encabezados)
        val colorUnselected = ContextCompat.getColor(this, R.color.grisOscuro)

        fun updateTab(position: Int) {
            navs.forEachIndexed { index, layout ->
                val icon = layout.getChildAt(0) as ImageView
                val label = layout.getChildAt(1) as TextView
                if (index == position) {
                    icon.setColorFilter(colorSelected)
                    label.setTextColor(colorSelected)
                } else {
                    icon.setColorFilter(colorUnselected)
                    label.setTextColor(colorUnselected)
                }
            }
        }

        // Estado inicial
        updateTab(0)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateTab(position)
            }
        })
    }
}
