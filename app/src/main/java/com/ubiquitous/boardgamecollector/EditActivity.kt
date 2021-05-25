package com.ubiquitous.boardgamecollector

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup

class EditActivity : AppCompatActivity() {

    private var id: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.edit)

        val extras = intent.extras ?: return
        id = extras.getInt("id")

        /*
        ScrollView
            name - plain text
            original_name - plain text
            year_published - spinner
                TODO: designers - generated radio buttons? (or hidden / uneditable???)
                TODO: artists - generated radio buttons? (or hidden / uneditable???)
            description - plain text
            date_ordered - date picker spinner
            date_added  - date picker spinner
            price_purchased - plain text
            rrp - plain text
            barcode - plain text
            bggid - uneditable (hidden?)
            mpn - plain text
            rank - uneditable (hidden?)
            base_expansion_status - choice out of 3 (or hidden)
            comment - plain text
            location - choice out of existing locations (if empty, then go to adding location)
            location_comment - plain text
            thumbnail - uneditable, hidden
        )
         */

        addLocationRadioButton(1, "Dom")
        addLocationRadioButton(2, "Szkoła")
        addLocationRadioButton(3, "Piwnica")
    }

    //takes location id from database and location name
    private fun addLocationRadioButton(id: Int, name: String){
        val group: RadioGroup = findViewById(R.id.locationRadioGroup)
        val radioButtonView: View = LayoutInflater.from(this).inflate(R.layout.radio_button, null, false)
        radioButtonView.tag = id
        val radioButton: RadioButton = radioButtonView.findViewById(R.id.radio_button)
        radioButton.text = name
        radioButton.id = id
        group.addView(radioButton)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.checkMark -> {

                //TODO: UPDATE DATABASE!!!

                val intent = Intent(this, DetailsActivity::class.java)
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra("id", id)
                intent.putExtra("edit", true)
                Log.i("goToDetailsActivity", "id=$id; edit=true")
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, DetailsActivity::class.java)
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("id", id)
        intent.putExtra("edit", false)
        Log.i("goToDetailsActivity", "id=$id; edit=false")
        startActivity(intent)
    }
}