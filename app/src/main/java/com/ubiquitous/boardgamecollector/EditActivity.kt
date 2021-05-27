package com.ubiquitous.boardgamecollector

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.children
import com.google.android.material.textfield.TextInputEditText
import java.time.LocalDate
import java.util.*


/*Layout:
    ScrollView
        name - plain text
        original_name - plain text
        year_published - spinner + null switch
            TODO: designers - generated radio buttons? (or hidden / uneditable???)
            TODO: artists - generated radio buttons? (or hidden / uneditable???)
        description - plain text
        date_ordered - date picker spinner + null switch
        date_added  - date picker spinner + null switch
        price_purchased - plain text
        rrp - plain text
        barcode - plain text
            TODO: bggid - plain text (uneditable / hidden?)
        mpn - plain text
        rank - uneditable (hidden?)
        base_expansion_status - choice out of 3 (or hidden)
            TODO: expansions (maybe only by loading from BGG?)
        comment - plain text
        location - choice out of existing locations (if empty, then go to adding location)
        location_comment - plain text
            TODO: thumbnail - add from phone memory?
    )
*/

//serves as both EDIT and ADD activity
class EditActivity : AppCompatActivity() {

    private var boardGameID = 0
    private var add = false

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val minYear = 1600
        val maxYear = 3000
        val minDate = Calendar.getInstance()
        minDate.set(minYear, 0, 1)
        val maxDate = Calendar.getInstance()
        maxDate.set(maxYear, 11, 31)
        val databaseHandler = DatabaseHandler.getInstance(this)

        val extras = intent.extras ?: return
        boardGameID = extras.getInt("id")

        // setting up edit/add activity
        val boardGame: BoardGame
        if (boardGameID == 0) {
            add = true
            supportActionBar?.setTitle(R.string.add_title)
            boardGame = BoardGame()
        } else {
            add = false
            supportActionBar?.setTitle(R.string.edit_title)
            boardGame = databaseHandler.getBoardGameDetails(boardGameID)
        }

        //initial filling with values

        val editName: TextInputEditText = findViewById(R.id.editName)
        editName.setText(boardGame.name)

        val editOriginalName: TextInputEditText = findViewById(R.id.editOriginalName)
        editOriginalName.setText(boardGame.originalName)

        val yearPicker: NumberPicker = findViewById(R.id.yearPicker)
        val yearPickerNull: SwitchCompat = findViewById(R.id.yearPickerNull)
        yearPicker.minValue = minYear
        yearPicker.maxValue = maxYear
        yearPicker.wrapSelectorWheel = true
        if (boardGame.yearPublished == null) {
            yearPicker.isEnabled = false
            yearPicker.value = LocalDate.now().year
            yearPickerNull.isChecked = true
        } else {
            yearPicker.isEnabled = true
            yearPicker.value = boardGame.yearPublished!!
            yearPickerNull.isChecked = false
        }

        val editDescription: TextInputEditText = findViewById(R.id.editDescription)
        editDescription.setText(boardGame.description)

        val dateOrderedPicker: DatePicker = findViewById(R.id.dateOrderedPicker)
        val dateOrderedPickerNull: SwitchCompat = findViewById(R.id.dateOrderedPickerNull)
        dateOrderedPicker.minDate = minDate.timeInMillis
        dateOrderedPicker.maxDate = maxDate.timeInMillis
        if (boardGame.dateOrdered == null) {
            dateOrderedPicker.isEnabled = false
            dateOrderedPicker.init(
                LocalDate.now().year,
                LocalDate.now().month.value - 1,
                LocalDate.now().dayOfMonth,
                null
            )
            dateOrderedPickerNull.isChecked = true
        } else {
            dateOrderedPicker.isEnabled = true
            dateOrderedPicker.init(
                boardGame.dateOrdered!!.year,
                boardGame.dateOrdered!!.month.value - 1,
                boardGame.dateOrdered!!.dayOfMonth,
                null
            )
            dateOrderedPickerNull.isChecked = false
        }

        val dateAddedPicker: DatePicker = findViewById(R.id.dateAddedPicker)
        val dateAddedPickerNull: SwitchCompat = findViewById(R.id.dateAddedPickerNull)
        dateAddedPicker.minDate = minDate.timeInMillis
        dateAddedPicker.maxDate = maxDate.timeInMillis
        if (boardGame.dateAdded == null) {
            dateAddedPicker.isEnabled = false
            dateAddedPicker.init(
                LocalDate.now().year,
                LocalDate.now().month.value - 1,
                LocalDate.now().dayOfMonth,
                null
            )
            dateAddedPickerNull.isChecked = true
        } else {
            dateAddedPicker.isEnabled = true
            dateAddedPicker.init(
                boardGame.dateAdded!!.year,
                boardGame.dateAdded!!.month.value - 1,
                boardGame.dateAdded!!.dayOfMonth,
                null
            )
            dateAddedPickerNull.isChecked = false
        }

        val editPricePurchased: TextInputEditText = findViewById(R.id.editPricePurchased)
        editPricePurchased.setText(boardGame.pricePurchased)

        val editRRP: TextInputEditText = findViewById(R.id.editRRP)
        editRRP.setText(boardGame.rrp)

        val editBarcode: TextInputEditText = findViewById(R.id.editBarcode)
        editBarcode.setText(boardGame.barcode)

        val editMPN: TextInputEditText = findViewById(R.id.editMPN)
        editMPN.setText(boardGame.mpn)

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
        editComment.setText(boardGame.comment)

        addLocationRadioButton(0, getString(R.string.noneSelected))
        val locations = databaseHandler.getAllLocations()
        for (loc in locations) {
            Log.i("LOCATION_" + loc.key, loc.value ?: getString(R.string.null_location_name))
            addLocationRadioButton(loc.key, loc.value ?: getString(R.string.null_location_name))
        }
        val locationID = databaseHandler.getLocationID(boardGameID)
        val radioButtonGroup: RadioGroup = findViewById(R.id.locationRadioGroup)
        for (view in radioButtonGroup.children) {
            if (view.tag == locationID) {
                val radioButton: RadioButton = view.findViewById(locationID)
                radioButton.isChecked = true
                break
            }
        }
        databaseHandler.close()
        loadLocationComment(radioButtonGroup) //unnecessary parameter

    }

    private fun addLocationRadioButton(locationID: Int, locationName: String) {
        val group: RadioGroup = findViewById(R.id.locationRadioGroup)
        val radioButtonView: View =
            LayoutInflater.from(this).inflate(R.layout.radio_button, null, false)
        radioButtonView.tag = locationID
        val radioButton: RadioButton = radioButtonView.findViewById(R.id.radio_button)
        radioButton.text = locationName
        radioButton.id = locationID
        group.addView(radioButton)
    }

    fun loadLocationComment(view: View) {
        val group: RadioGroup = findViewById(R.id.locationRadioGroup)
        val locationID = group.checkedRadioButtonId
        val editLocationComment: TextInputEditText = findViewById(R.id.editLocationComment)

        if (locationID > 0) {
            val databaseHandler = DatabaseHandler.getInstance(this)
            editLocationComment.isEnabled = true
            editLocationComment.setText(databaseHandler.getLocationComment(boardGameID, locationID))
            databaseHandler.close()
        } else {
            editLocationComment.isEnabled = false
            editLocationComment.text = null
        }
    }

    fun disableYearPicker(view: View) {
        val switch = view as SwitchCompat
        val yearPicker: NumberPicker = findViewById(R.id.yearPicker)
        yearPicker.isEnabled = !switch.isChecked
    }

    fun disableDateOrderedPicker(view: View) {
        val switch = view as SwitchCompat
        val dateOrderedPicker: DatePicker = findViewById(R.id.dateOrderedPicker)
        dateOrderedPicker.isEnabled = !switch.isChecked
    }

    fun disableDateAddedPicker(view: View) {
        val switch = view as SwitchCompat
        val dateAddedPicker: DatePicker = findViewById(R.id.dateAddedPicker)
        dateAddedPicker.isEnabled = !switch.isChecked
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if (add) {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.putExtra("id", boardGameID)
                    intent.putExtra("add", add)
                    Log.i("onBackPressed_CancelAdd", "id=$boardGameID; add=$add")
                    startActivity(intent)
                } else {
                    onBackPressed()
                }
                true
            }
            R.id.checkMark -> {

                //UPDATE DATABASE
                val databaseHandler = DatabaseHandler.getInstance(this)
                val boardGame = BoardGame()
                boardGame.id = boardGameID

                val editName: TextInputEditText = findViewById(R.id.editName)
                boardGame.name = when (editName.text.toString().trim()) {
                    "" -> null
                    else -> editName.text.toString()
                }

                val editOriginalName: TextInputEditText = findViewById(R.id.editOriginalName)
                boardGame.originalName = when (editOriginalName.text.toString().trim()) {
                    "" -> null
                    else -> editOriginalName.text.toString()
                }

                val yearPicker: NumberPicker = findViewById(R.id.yearPicker)
                val yearPickerNull: SwitchCompat = findViewById(R.id.yearPickerNull)
                boardGame.yearPublished = when (yearPickerNull.isChecked) {
                    true -> null
                    else -> yearPicker.value
                }

                val editDescription: TextInputEditText = findViewById(R.id.editDescription)
                boardGame.description = when (editDescription.text.toString().trim()) {
                    "" -> null
                    else -> editDescription.text.toString()
                }

                val dateOrderedPicker: DatePicker = findViewById(R.id.dateOrderedPicker)
                val dateOrderedPickerNull: SwitchCompat = findViewById(R.id.dateOrderedPickerNull)
                boardGame.dateOrdered = when (dateOrderedPickerNull.isChecked) {
                    true -> null
                    else -> LocalDate.of(
                        dateOrderedPicker.year,
                        dateOrderedPicker.month + 1,
                        dateOrderedPicker.dayOfMonth
                    )
                }

                val dateAddedPicker: DatePicker = findViewById(R.id.dateAddedPicker)
                val dateAddedPickerNull: SwitchCompat = findViewById(R.id.dateAddedPickerNull)
                boardGame.dateAdded = when (dateAddedPickerNull.isChecked) {
                    true -> null
                    else -> LocalDate.of(
                        dateAddedPicker.year,
                        dateAddedPicker.month + 1,
                        dateAddedPicker.dayOfMonth
                    )
                }

                val editPricePurchased: TextInputEditText = findViewById(R.id.editPricePurchased)
                boardGame.pricePurchased = when (editPricePurchased.text.toString().trim()) {
                    "" -> null
                    else -> editPricePurchased.text.toString()
                }

                val editRRP: TextInputEditText = findViewById(R.id.editRRP)
                boardGame.rrp = when (editRRP.text.toString().trim()) {
                    "" -> null
                    else -> editRRP.text.toString()
                }

                val editBarcode: TextInputEditText = findViewById(R.id.editBarcode)
                boardGame.barcode = when (editBarcode.text.toString().trim()) {
                    "" -> null
                    else -> editBarcode.text.toString()
                }

                val editMPN: TextInputEditText = findViewById(R.id.editMPN)
                boardGame.mpn = when (editMPN.text.toString().trim()) {
                    "" -> null
                    else -> editMPN.text.toString()
                }

                val expansionRadioButton: RadioButton = findViewById(R.id.expansionRadioButton)
                val bothRadioButton: RadioButton = findViewById(R.id.bothRadioButton)
                boardGame.baseExpansionStatus = when {
                    expansionRadioButton.isChecked -> BaseExpansionStatus.EXPANSION
                    bothRadioButton.isChecked -> BaseExpansionStatus.BOTH
                    else -> BaseExpansionStatus.BASE
                }

                val editComment: TextInputEditText = findViewById(R.id.editComment)
                boardGame.comment = when (editComment.text.toString().trim()) {
                    "" -> null
                    else -> editComment.text.toString()
                }

                val radioButtonGroup: RadioGroup = findViewById(R.id.locationRadioGroup)
                val locationID = radioButtonGroup.checkedRadioButtonId

                val editLocationComment: TextInputEditText = findViewById(R.id.editLocationComment)
                boardGame.locationComment = when (editLocationComment.text.toString().trim()) {
                    "" -> null
                    else -> editLocationComment.text.toString()
                }

                if (add) {
                    boardGameID = databaseHandler.insertBoardGame(boardGame, locationID)
                } else {
                    databaseHandler.updateBoardGame(boardGame, locationID)
                }
                databaseHandler.close()

                val intent = Intent(this, DetailsActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra("id", boardGameID)
                intent.putExtra("edit", true)
                intent.putExtra("add", add)
                Log.i("goToDetailsActivity", "id=$boardGameID; edit=true; add=$add")
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, DetailsActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("id", boardGameID)
        intent.putExtra("edit", false)
        intent.putExtra("add", add)
        Log.i("goToDetailsActivity", "id=$boardGameID; edit=false; add=$add")
        startActivity(intent)
    }
}