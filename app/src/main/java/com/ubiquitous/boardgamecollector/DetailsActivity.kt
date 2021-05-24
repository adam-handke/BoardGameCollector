package com.ubiquitous.boardgamecollector

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

class DetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setLogo(R.mipmap.ic_launcher)
        supportActionBar?.setDisplayUseLogoEnabled(true)

        val extras = intent.extras ?: return
        val id = extras.getInt("id")

        val toast = Toast.makeText(applicationContext, "ID = $id", Toast.LENGTH_SHORT)
        toast.show()
    }
}