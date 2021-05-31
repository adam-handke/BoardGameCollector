package com.ubiquitous.boardgamecollector

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.*


class LocationsActivity : AppCompatActivity() {

    private lateinit var locationsListView: ListView
    private var list: MutableList<Triple<Int, String?, List<String>>> =
        mutableListOf() //(location_id, location_name, list_game_names)
    private var editMode = false
    private var editedLocationID = 0

    private fun displayLocations() {
        list.clear()
        val databaseHandler = DatabaseHandler.getInstance(this)
        val locationMap = databaseHandler.getAllLocations()
        for (location in locationMap) {
            val boardGamesInLocation =
                databaseHandler.getAllBoardGamesInLocation(location.key).sortedBy {
                    it.nameToString("~").toLowerCase(Locale.ROOT)
                }.map {
                    it.nameToString(getString(R.string.unnamed_board_game))
                }
            list.add(Triple(location.key, location.value, boardGamesInLocation))
        }
        databaseHandler.close()

        supportActionBar?.subtitle = getString(R.string.locations_subtitle, list.size)

        val rows = ArrayList<HashMap<String, String>>()
        if (list.isNotEmpty()) {
            for (element in list) {
                val item = HashMap<String, String>()
                item["location"] = element.second ?: getString(R.string.unnamed_location)
                item["games"] = when (element.third.size) {
                    0 -> getString(R.string.location_no_games)
                    else -> element.third.joinToString(", \n")
                }
                rows.add(item)
            }
            val adapter = SimpleAdapter(
                this,
                rows,
                android.R.layout.simple_list_item_2,
                arrayOf("location", "games"),
                intArrayOf(android.R.id.text1, android.R.id.text2)
            )
            locationsListView.adapter = adapter
        } else {
            //clear list view if no locations
            locationsListView.adapter = SimpleAdapter(
                this,
                ArrayList<HashMap<String, String>>(),
                android.R.layout.simple_list_item_2,
                arrayOf("location", "games"),
                intArrayOf(android.R.id.text1, android.R.id.text2)
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_locations)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.locations_title)

        editMode = false
        locationsListView = findViewById(R.id.locationsList)

        //insert an empty footer in order not to cover the last row with the buttons
        val emptyTextView = TextView(this)
        emptyTextView.height = (resources.displayMetrics.density * 64 + 0.5).toInt()
        emptyTextView.isClickable = false
        locationsListView.addFooterView(emptyTextView, null, false)

        val textInputLayout: TextInputLayout = findViewById(R.id.locationHint)
        textInputLayout.hint = getString(R.string.add_location)
        val locationNameEditText: TextInputEditText = findViewById(R.id.locationName)

        //ListView item click events:
        // onClick - edit location name
        // onLongClick - delete location if empty
        locationsListView.setOnItemClickListener { _, _, position, _ ->
            //TODO: highlight clicked list item
            if(position < list.size){
                editMode = true
                textInputLayout.hint = getString(R.string.edit_location)
                locationNameEditText.setText(list[position].second ?: "")
                editedLocationID = list[position].first
            }
        }

        locationsListView.setOnItemLongClickListener { _, _, position, _ ->
            if (position < list.size) {
                editMode = false
                if (list[position].third.isEmpty()) {
                    val databaseHandler = DatabaseHandler.getInstance(this)
                    databaseHandler.deleteLocation(list[position].first)
                    databaseHandler.close()
                    val toast = Toast.makeText(
                        applicationContext,
                        getString(R.string.location_deleted),
                        Toast.LENGTH_SHORT
                    )
                    toast.show()
                    displayLocations()
                } else {
                    val toast = Toast.makeText(
                        applicationContext,
                        getString(R.string.location_cant_delete),
                        Toast.LENGTH_SHORT
                    )
                    toast.show()
                }
            }
            true
        }

        displayLocations()
    }

    fun locationConfirm(view: View) {
        val textInputLayout: TextInputLayout = findViewById(R.id.locationHint)
        val locationNameEditText: TextInputEditText = findViewById(R.id.locationName)

        var toastString: String = getString(R.string.error)
        if (locationNameEditText.text.isNullOrBlank()) {
            toastString = getString(R.string.location_wrong_name)
        } else {
            if (editMode) {
                //edit an existing location
                if (editedLocationID > 0) {
                    val databaseHandler = DatabaseHandler.getInstance(this)
                    val rows = databaseHandler.updateLocationName(
                        editedLocationID,
                        locationNameEditText.text.toString().trim()
                    )
                    databaseHandler.close()
                    if (rows == 1) {
                        toastString = getString(R.string.location_edited)
                    }
                }
            } else {
                //add a new location
                val databaseHandler = DatabaseHandler.getInstance(this)
                val locID =
                    databaseHandler.insertLocation(locationNameEditText.text.toString().trim())
                databaseHandler.close()
                if (locID > 0) {
                    toastString = getString(R.string.location_added)
                }
            }
        }
        val toast = Toast.makeText(
            applicationContext,
            toastString,
            Toast.LENGTH_SHORT
        )
        toast.show()

        editedLocationID = 0
        editMode = false
        textInputLayout.hint = getString(R.string.add_location)
        locationNameEditText.text?.clear()
        displayLocations()
    }

    fun locationCancel(view: View) {
        editMode = false
        editedLocationID = 0
        val textInputLayout: TextInputLayout = findViewById(R.id.locationHint)
        textInputLayout.hint = getString(R.string.add_location)
        val locationNameEditText: TextInputEditText = findViewById(R.id.locationName)
        locationNameEditText.text?.clear()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //same blank menu as RankHistoryActivity with only go-back arrow
        menuInflater.inflate(R.menu.menu_rank_history_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
            else -> {
            }
        }
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        Log.i("onBackPressed", "")
        startActivity(intent)
    }
}