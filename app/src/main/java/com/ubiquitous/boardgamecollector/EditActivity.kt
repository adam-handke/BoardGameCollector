package com.ubiquitous.boardgamecollector

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import android.widget.NumberPicker
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.view.children
import com.google.android.material.textfield.TextInputEditText
import java.time.Instant
import java.time.LocalDate
import java.util.*
import kotlin.math.exp


/*Layout:
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

class EditActivity : AppCompatActivity() {

    private var id = 0

    //TODO: nullable numberpicker and datepickers (null checkbox?)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.edit)

        val extras = intent.extras ?: return
        id = extras.getInt("id")

        val minYear = 1600
        val maxYear = 3000
        val minDate = Calendar.getInstance()
        minDate.set(minYear, 0, 1)
        val maxDate = Calendar.getInstance()
        maxDate.set(maxYear, 11, 31)

        //initial filling with values
        val databaseHandler = DatabaseHandler.getInstance(this)
        val boardGame = databaseHandler.getBoardGameByID(id)

        val editName: TextInputEditText = findViewById(R.id.editName)
        editName.setText(boardGame.name ?: "")

        val editOriginalName: TextInputEditText = findViewById(R.id.editOriginalName)
        editOriginalName.setText(boardGame.originalName ?: "")

        val yearPicker: NumberPicker = findViewById(R.id.yearPicker)
        yearPicker.maxValue = maxYear
        yearPicker.minValue = minYear
        yearPicker.wrapSelectorWheel = true
        if (boardGame.yearPublished == null) {
            yearPicker.value = LocalDate.now().year
        } else {
            yearPicker.value = boardGame.yearPublished!!
        }

        val editDescription: TextInputEditText = findViewById(R.id.editDescription)
        editDescription.setText(boardGame.description ?: "")

        val dateOrderedPicker: DatePicker = findViewById(R.id.dateOrderedPicker)
        dateOrderedPicker.minDate = minDate.timeInMillis
        dateOrderedPicker.maxDate = maxDate.timeInMillis
        if (boardGame.dateOrdered == null) {
            dateOrderedPicker.init(
                LocalDate.now().year,
                LocalDate.now().month.value - 1,
                LocalDate.now().dayOfMonth,
                null
            )
        } else {
            dateOrderedPicker.init(
                boardGame.dateOrdered!!.year,
                boardGame.dateOrdered!!.month.value - 1,
                boardGame.dateOrdered!!.dayOfMonth,
                null
            )
        }

        val dateAddedPicker: DatePicker = findViewById(R.id.dateAddedPicker)
        dateAddedPicker.minDate = minDate.timeInMillis
        dateAddedPicker.maxDate = maxDate.timeInMillis
        if (boardGame.dateAdded == null) {
            dateAddedPicker.init(
                LocalDate.now().year,
                LocalDate.now().month.value - 1,
                LocalDate.now().dayOfMonth,
                null
            )
        } else {
            dateAddedPicker.init(
                boardGame.dateAdded!!.year,
                boardGame.dateAdded!!.month.value - 1,
                boardGame.dateAdded!!.dayOfMonth,
                null
            )
        }

        val editPricePurchased: TextInputEditText = findViewById(R.id.editPricePurchased)
        editPricePurchased.setText(boardGame.pricePurchased ?: "")

        val editRRP: TextInputEditText = findViewById(R.id.editRRP)
        editRRP.setText(boardGame.rrp ?: "")

        val editBarcode: TextInputEditText = findViewById(R.id.editBarcode)
        editBarcode.setText(boardGame.barcode ?: "")

        val editMPN: TextInputEditText = findViewById(R.id.editMPN)
        editMPN.setText(boardGame.mpn ?: "")

        val baseRadioButton: RadioButton = findViewById(R.id.baseRadioButton)
        val expansionRadioButton: RadioButton = findViewById(R.id.expansionRadioButton)
        val bothRadioButton: RadioButton = findViewById(R.id.bothRadioButton)
        when (boardGame.baseExpansionStatus) {
            BaseExpansionStatus.EXPANSION -> {
                expansionRadioButton.isChecked = true
            }
            BaseExpansionStatus.BOTH -> {
                bothRadioButton.isChecked = true
            }
            else -> {
                baseRadioButton.isChecked = true
            }
        }

        val editComment: TextInputEditText = findViewById(R.id.editComment)
        editComment.setText(boardGame.comment ?: "")

        addLocationRadioButton(0, getString(R.string.nullName))
        val locations = databaseHandler.getAllLocations()
        for (loc in locations) {
            Log.i("LOCATION_" + loc.key, loc.value ?: getString(R.string.nullName))
            addLocationRadioButton(loc.key, loc.value ?: getString(R.string.nullName))
        }
        val locationID = databaseHandler.getLocationIDByBoardGameID(id)
        val radioButtonGroup: RadioGroup = findViewById(R.id.locationRadioGroup)
        for (view in radioButtonGroup.children) {
            if (view.tag == locationID) {
                val radioButton: RadioButton = view.findViewById(locationID)
                radioButton.isChecked = true
                break
            }
        }

        val editLocationComment: TextInputEditText = findViewById(R.id.editLocationComment)
        editLocationComment.setText(boardGame.locationComment ?: "")

        databaseHandler.close()
    }

    //takes location id from database and location name
    private fun addLocationRadioButton(id: Int, name: String) {
        val group: RadioGroup = findViewById(R.id.locationRadioGroup)
        val radioButtonView: View =
            LayoutInflater.from(this).inflate(R.layout.radio_button, null, false)
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
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.checkMark -> {

                //UPDATE DATABASE
                //TODO: nullness
                val databaseHandler = DatabaseHandler.getInstance(this)
                val boardGame = BoardGame()
                boardGame.id = id

                val editName: TextInputEditText = findViewById(R.id.editName)
                boardGame.name = editName.text.toString()

                val editOriginalName: TextInputEditText = findViewById(R.id.editOriginalName)
                boardGame.originalName = editOriginalName.text.toString()

                val yearPicker: NumberPicker = findViewById(R.id.yearPicker)
                boardGame.yearPublished = yearPicker.value

                val editDescription: TextInputEditText = findViewById(R.id.editDescription)
                boardGame.description = editDescription.text.toString()

                val dateOrderedPicker: DatePicker = findViewById(R.id.dateOrderedPicker)
                boardGame.dateOrdered = LocalDate.of(
                    dateOrderedPicker.year,
                    dateOrderedPicker.month + 1,
                    dateOrderedPicker.dayOfMonth
                )

                val dateAddedPicker: DatePicker = findViewById(R.id.dateAddedPicker)
                boardGame.dateAdded = LocalDate.of(
                    dateAddedPicker.year,
                    dateAddedPicker.month + 1,
                    dateAddedPicker.dayOfMonth
                )

                val editPricePurchased: TextInputEditText = findViewById(R.id.editPricePurchased)
                boardGame.pricePurchased = editPricePurchased.text.toString()

                val editRRP: TextInputEditText = findViewById(R.id.editRRP)
                boardGame.rrp = editRRP.text.toString()

                val editBarcode: TextInputEditText = findViewById(R.id.editBarcode)
                boardGame.barcode = editBarcode.text.toString()

                val editMPN: TextInputEditText = findViewById(R.id.editMPN)
                boardGame.mpn = editMPN.text.toString()

                val expansionRadioButton: RadioButton = findViewById(R.id.expansionRadioButton)
                val bothRadioButton: RadioButton = findViewById(R.id.bothRadioButton)
                if (expansionRadioButton.isChecked) {
                    boardGame.baseExpansionStatus = BaseExpansionStatus.EXPANSION
                } else if (bothRadioButton.isChecked) {
                    boardGame.baseExpansionStatus = BaseExpansionStatus.BOTH
                } else {
                    boardGame.baseExpansionStatus = BaseExpansionStatus.BASE
                }

                val editComment: TextInputEditText = findViewById(R.id.editComment)
                boardGame.comment = editComment.text.toString()

                var locationID: Int = 0
                val radioButtonGroup: RadioGroup = findViewById(R.id.locationRadioGroup)
                locationID = radioButtonGroup.checkedRadioButtonId

                val editLocationComment: TextInputEditText = findViewById(R.id.editLocationComment)
                boardGame.locationComment = editLocationComment.text.toString()

                databaseHandler.updateBoardGame(boardGame, locationID)
                databaseHandler.close()

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