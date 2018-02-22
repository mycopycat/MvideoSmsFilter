package com.liarstudio.mvideosmsfilter

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Environment
import android.support.annotation.IdRes
import android.support.design.widget.TabLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast

import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    private var toolbar: Toolbar? = null
    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        //ActionBar actionBar = getSupportActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(true);

        viewPager = findViewById(R.id.viewpager) as ViewPager
        setupViewPager(viewPager)

        tabLayout = findViewById(R.id.tablayout) as TabLayout
        tabLayout!!.setupWithViewPager(viewPager)
    }

    private fun setupViewPager(viewPager: ViewPager?) {
        val adapter = PagerAdapterTabs(supportFragmentManager)
        adapter.addItem(SendFragment(), "Отправка")
        adapter.addItem(SaveFragment(), "Сохранение")
        viewPager!!.adapter = adapter
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        /*List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments)
                fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }*/
    }


}
