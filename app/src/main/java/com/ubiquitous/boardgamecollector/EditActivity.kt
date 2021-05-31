package com.ubiquitous.boardgamecollector

import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.children
import com.google.android.material.textfield.TextInputEditText
import java.time.LocalDate
import java.util.*


/*Layout:
    ScrollView
        name - AutoCompleteTextView (auto-complete with OriginalName or alternate names from BGG)
        original_name - TextInputEditText
        year_published - spinner + null switch
            TODO: designers - generated radio buttons? (or hidden / uneditable???)
            TODO: artists - generated radio buttons? (or hidden / uneditable???)
        description - TextInputEditText
        date_ordered - date picker spinner + null switch
        date_added  - date picker spinner + null switch
        price_purchased - TextInputEditText
        rrp - TextInputEditText
        barcode - TextInputEditText
        bggid - TextInputEditText, number-type (if null, then 0)
        mpn - TextInputEditText
        rank - uneditable (hidden)
        base_expansion_status - choice out of 3 (or hidden)
            TODO: edit expansions (or only by loading from BGG?)
        comment - TextInputEditText
        location - choice out of existing locations (if empty, then go to adding location)
        location_comment - TextInputEditText
            TODO: thumbnail - add from phone memory?
    )
*/

//serves as both EDIT and ADD activity
class EditActivity : AppCompatActivity() {

    private var boardGame = BoardGame()
    private var add = false

    @SuppressLint("ClickableViewAccessibility")
    private fun initializeFields() {
        supportActionBar?.subtitle = boardGame.nameToString(getString(R.string.unnamed_board_game))

        Log.i(
            "initializeFields",
            "id=${boardGame.id}; bggid=${boardGame.bggid}; rank=${boardGame.rank}"
        )
        val minYear = 0
        val maxYear = 3000
        val minDate = Calendar.getInstance()
        minDate.set(minYear, 0, 1)
        val maxDate = Calendar.getInstance()
        maxDate.set(maxYear, 11, 31)
        val databaseHandler = DatabaseHandler.getInstance(this)

        //initial filling with values
        val editName: AutoCompleteTextView = findViewById(R.id.editName)
        editName.setText(boardGame.name)
        //set autocomplete for main name
        val alternateNames: List<String> = if (boardGame.originalName != null) {
            //original name as first alternate name
            listOf(boardGame.originalName!!) + boardGame.alternateNames
        } else {
            boardGame.alternateNames
        }
        if (alternateNames.isNotEmpty()) {
            val adapter =
                ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, alternateNames)
            editName.setAdapter(adapter)
            editName.threshold = 0

            editName.setOnTouchListener { _, _ ->
                editName.showDropDown()
                false
            }
        }

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

        val editBGGID: TextInputEditText = findViewById(R.id.editBGGID)
        editBGGID.setText(boardGame.bggid.toString())

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

        val locations = databaseHandler.getAllLocations()
        val radioButtonGroup: RadioGroup = findViewById(R.id.locationRadioGroup)
        //remove all radio button before adding new
        val count: Int = radioButtonGroup.childCount
        if (count > 0) {
            for (i in count - 1 downTo 0) {
                val o: View = radioButtonGroup.getChildAt(i)
                if (o is RadioButton) {
                    radioButtonGroup.removeViewAt(i)
                }
            }
        }
        addLocationRadioButton(0, getString(R.string.noneSelected))
        for (loc in locations) {
            Log.i("LOCATION_" + loc.key, loc.value ?: getString(R.string.null_location_name))
            addLocationRadioButton(loc.key, loc.value ?: getString(R.string.null_location_name))
        }
        val locationID = databaseHandler.getLocationID(boardGame.id ?: 0)
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

    //async task for loading data from BGG API (by game bggid)
    inner class APIAsyncTask : AsyncTask<Int, Int, BoardGame>() {

        override fun onPreExecute() {
            super.onPreExecute()
            val editLayout: LinearLayout = findViewById(R.id.editLayout)
            editLayout.isEnabled = false

            val toast = Toast.makeText(
                applicationContext,
                getString(R.string.loading_board_game),
                Toast.LENGTH_SHORT
            )
            toast.show()
        }

        override fun doInBackground(vararg params: Int?): BoardGame {
            val bggid = params[0]
            return try {
                BoardGameGeek().loadBoardGame(bggid ?: 0)
            } catch (e: Exception) {
                Log.e(
                    "doInBackground_EXCEPTION",
                    "bggid=$bggid; ${e.message}; ${e.stackTraceToString()}"
                )
                BoardGame()
            }
        }

        override fun onPostExecute(result: BoardGame) {
            super.onPostExecute(result)
            val editLayout: LinearLayout = findViewById(R.id.editLayout)
            editLayout.isEnabled = false
            boardGame = result

            initializeFields()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val extras = intent.extras ?: return
        val id = extras.getInt("id")
        val bggid = extras.getInt("bggid")

        // setting up edit/add/addBGG activity
        val databaseHandler = DatabaseHandler.getInstance(this)
        if (id == 0) {
            add = true
            supportActionBar?.setTitle(R.string.add_title)
            if (bggid > 0) {
                APIAsyncTask().execute(bggid)
            } else {
                boardGame = BoardGame()
                initializeFields()
            }
        } else {
            add = false
            supportActionBar?.setTitle(R.string.edit_title)
            boardGame = databaseHandler.getBoardGameDetails(id)
            initializeFields()
        }
        databaseHandler.close()
    }

    @SuppressLint("InflateParams")
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
            editLocationComment.setText(
                databaseHandler.getLocationComment(
                    boardGame.id ?: 0,
                    locationID
                )
            )
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
                    intent.putExtra("id", boardGame.id)
                    intent.putExtra("add", add)
                    Log.i(
                        "onBackPressed_CancelAdd",
                        "id=${boardGame.id}; bggid={${boardGame.bggid}; add=$add"
                    )
                    startActivity(intent)
                } else {
                    onBackPressed()
                }
                true
            }
            R.id.checkMark -> {
                //TODO: on ADD BGG mode: insert artists, designers

                //UPDATE DATABASE
                val databaseHandler = DatabaseHandler.getInstance(this)

                val editName: AutoCompleteTextView = findViewById(R.id.editName)
                boardGame.name = when (editName.text.toString().trim()) {
                    "" -> null
                    else -> editName.text.toString().trim()
                }

                val editOriginalName: TextInputEditText = findViewById(R.id.editOriginalName)
                boardGame.originalName = when (editOriginalName.text.toString().trim()) {
                    "" -> null
                    else -> editOriginalName.text.toString().trim()
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
                    else -> editDescription.text.toString().trim()
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
                    else -> editPricePurchased.text.toString().trim()
                }

                val editRRP: TextInputEditText = findViewById(R.id.editRRP)
                boardGame.rrp = when (editRRP.text.toString().trim()) {
                    "" -> null
                    else -> editRRP.text.toString().trim()
                }

                val editBarcode: TextInputEditText = findViewById(R.id.editBarcode)
                boardGame.barcode = when (editBarcode.text.toString().trim()) {
                    "" -> null
                    else -> editBarcode.text.toString().trim()
                }

                val editBGGID: TextInputEditText = findViewById(R.id.editBGGID)
                boardGame.bggid = when (editBGGID.text.toString().trim()) {
                    "" -> 0
                    else -> editBGGID.text.toString().trim().toInt()
                }

                val editMPN: TextInputEditText = findViewById(R.id.editMPN)
                boardGame.mpn = when (editMPN.text.toString().trim()) {
                    "" -> null
                    else -> editMPN.text.toString().trim()
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
                    else -> editComment.text.toString().trim()
                }

                val radioButtonGroup: RadioGroup = findViewById(R.id.locationRadioGroup)
                val locationID = radioButtonGroup.checkedRadioButtonId

                val editLocationComment: TextInputEditText = findViewById(R.id.editLocationComment)
                boardGame.locationComment = when (editLocationComment.text.toString().trim()) {
                    "" -> null
                    else -> editLocationComment.text.toString().trim()
                }

                //TODO: add artists & designers when adding manually
                if (add) {
                    boardGame.id = databaseHandler.insertBoardGame(boardGame, locationID)
                } else {
                    databaseHandler.updateBoardGame(boardGame, locationID)
                }
                databaseHandler.close()

                val intent = Intent(this, DetailsActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra("id", boardGame.id)
                intent.putExtra("edit", true)
                intent.putExtra("add", add)
                Log.i(
                    "goToDetailsActivity",
                    "id=${boardGame.id}; bggid=${boardGame.bggid}; edit=true; add=$add"
                )
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
        intent.putExtra("id", boardGame.id)
        intent.putExtra("edit", false)
        intent.putExtra("add", add)
        Log.i(
            "goToDetailsActivity",
            "id=${boardGame.id}; bggid={${boardGame.bggid}; edit=false; add=$add"
        )
        startActivity(intent)
    }
}