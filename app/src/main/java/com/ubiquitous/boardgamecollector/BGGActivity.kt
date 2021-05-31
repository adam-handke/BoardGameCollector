package com.ubiquitous.boardgamecollector

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.textfield.TextInputEditText
import java.lang.Exception

class BGGActivity : AppCompatActivity() {
    private var searchPhrase: String? = null
    private var apiAsyncTask: APIAsyncTask = APIAsyncTask()
    private var mapPositionBGGID: MutableMap<Int, Int> = mutableMapOf()
    private var addAll: Boolean = false
    private val context = this

    //displaying search results as
    fun displaySearchResults(
        asyncSearchName: Boolean,
        list: List<BoardGame>,
        longLoadingWarningAmount: Int? = null
    ) {
        mapPositionBGGID.clear()
        val searchResultsListView: ListView = findViewById(R.id.searchResultsList)
        val stringList = if (longLoadingWarningAmount != null) {
            //display warning that *longLoadingWarningAmount* results are being loaded
            listOf(getString(R.string.search_long_loading_warning, longLoadingWarningAmount))
        } else {
            supportActionBar?.subtitle = getString(R.string.search_results, list.size)
            if (list.isNotEmpty()) {
                for (i in list.indices) {
                    mapPositionBGGID[i] = list[i].bggid
                }
                list.map { it.nameToString(getString(R.string.unnamed_board_game)) }
            } else {
                if (asyncSearchName) {
                    listOf(getString(R.string.search_name_not_found))
                } else {
                    listOf(getString(R.string.search_username_not_found))
                }
            }
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, stringList)
        searchResultsListView.adapter = adapter
    }

    //async task for loading data from BGG API (by game name or by username)
    //if addAll==true, then instead adds all search results to database
    inner class APIAsyncTask : AsyncTask<Boolean, Int, Pair<Boolean, List<BoardGame>>>() {
        var isRunning = false

        override fun doInBackground(vararg params: Boolean?): Pair<Boolean, List<BoardGame>> {
            isRunning = true
            if (!addAll) {
                //async searching
                val asyncSearchName = params[0] ?: true //search by: true=game name; false=username
                val searchPhraseView: TextInputEditText = findViewById(R.id.searchPhrase)
                val searchPhrase: String = searchPhraseView.text.toString().trim()
                return try {
                    when (asyncSearchName) {
                        true -> Pair(
                            asyncSearchName,
                            BoardGameGeek().searchBoardGamesByName(searchPhrase, this)
                        )
                        else -> Pair(
                            asyncSearchName,
                            BoardGameGeek().searchBoardGamesByUsername(searchPhrase, this)
                        )
                    }
                } catch (e: Exception) {
                    Log.e(
                        "APIAsyncTask_doInBackground_EXCEPTION",
                        "search_phrase=$searchPhrase; ${e.message}; ${e.stackTraceToString()}"
                    )
                    Pair(asyncSearchName, listOf())
                }
            } else {
                //async adding all search results
                val databaseHandler = DatabaseHandler.getInstance(context)
                for (bggid in mapPositionBGGID.values) {
                    if (!isCancelled) {
                        try {
                            databaseHandler.insertBoardGame(BoardGameGeek().loadBoardGame(bggid), 0)
                        } catch (e: Exception) {
                            Log.e(
                                "APIAsyncTask_ADD_ALL",
                                "bggid=$bggid; ${e.message}; ${e.stackTraceToString()}"
                            )
                        }
                    }
                }
                databaseHandler.close()

                return Pair(true, listOf())
            }
        }

        //display warning that *amount* results are being loaded
        fun publicPublishProgress(amount: Int) {
            publishProgress(amount)
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            displaySearchResults(true, listOf(), values[0])
        }

        override fun onCancelled() {
            super.onCancelled()
            if (isRunning && !addAll) {
                val toast = Toast.makeText(
                    applicationContext,
                    getString(R.string.search_cancelled),
                    Toast.LENGTH_SHORT
                )
                toast.show()
                //isRunning = false
            } else if (addAll) {
                addAll = false
            }
        }

        override fun onPostExecute(result: Pair<Boolean, List<BoardGame>>) {
            super.onPostExecute(result)
            isRunning = false
            if (!addAll) {
                displaySearchResults(result.first, result.second, null)
            } else {
                val toast = Toast.makeText(
                    applicationContext,
                    getString(R.string.added_X_games, mapPositionBGGID.keys.size),
                    Toast.LENGTH_SHORT
                )
                toast.show()

                addAll = false
                val searchPhraseView: TextInputEditText = findViewById(R.id.searchPhrase)
                searchPhraseView.isEnabled = true
                val searchNameButton: Button = findViewById(R.id.searchName)
                searchNameButton.isEnabled = true
                val searchUsernameButton: Button = findViewById(R.id.searchUsername)
                searchUsernameButton.isEnabled = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bgg)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.add_bgg_title)

        val searchResultsListView: ListView = findViewById(R.id.searchResultsList)
        searchResultsListView.setOnItemClickListener { _, _, position, _ ->
            if (mapPositionBGGID.isNotEmpty() && !addAll) {
                //go to edit activity to add a board game by BGGID
                apiAsyncTask.cancel(true)
                val bggid = mapPositionBGGID[position]

                val intent = Intent(this, EditActivity::class.java)
                intent.putExtra("id", 0)
                intent.putExtra("bggid", bggid)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                Log.i("goToEditActivity_ADD_BGG", "id=0; bggid=$bggid")
                startActivity(intent)
            }
        }

        //insert an empty footer in order not to cover the last row with the buttons
        val emptyTextView = TextView(this)
        emptyTextView.height = (resources.displayMetrics.density * 64 + 0.5).toInt()
        emptyTextView.isClickable = false
        searchResultsListView.addFooterView(emptyTextView, null, false)
    }

    fun searchName(view: View) {
        apiAsyncTask.cancel(true)
        apiAsyncTask = APIAsyncTask()
        apiAsyncTask.execute(true)
    }

    fun searchUsername(view: View) {
        apiAsyncTask.cancel(true)
        apiAsyncTask = APIAsyncTask()
        apiAsyncTask.execute(false)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_bgg_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
            R.id.add_all -> {
                if (mapPositionBGGID.isNotEmpty() && !addAll) {
                    addAll = true
                    val searchPhraseView: TextInputEditText = findViewById(R.id.searchPhrase)
                    searchPhraseView.isEnabled = false
                    val searchNameButton: Button = findViewById(R.id.searchName)
                    searchNameButton.isEnabled = false
                    val searchUsernameButton: Button = findViewById(R.id.searchUsername)
                    searchUsernameButton.isEnabled = false
                    val toast = Toast.makeText(
                        applicationContext,
                        getString(R.string.search_add_all_wait, mapPositionBGGID.keys.size),
                        Toast.LENGTH_LONG
                    )
                    toast.show()

                    //async adding all search results
                    apiAsyncTask.cancel(true)
                    apiAsyncTask = APIAsyncTask()
                    apiAsyncTask.execute()
                } else if (addAll) {
                    val toast = Toast.makeText(
                        applicationContext,
                        getString(R.string.search_add_all_wait, mapPositionBGGID.keys.size),
                        Toast.LENGTH_LONG
                    )
                    toast.show()
                } else {
                    val toast = Toast.makeText(
                        applicationContext,
                        getString(R.string.search_add_all_empty),
                        Toast.LENGTH_SHORT
                    )
                    toast.show()
                }
            }
            else -> {
            }
        }
        return true
    }

    override fun onBackPressed() {
        apiAsyncTask.cancel(true)
        if (addAll) {
            //addAll = false
            val toast = Toast.makeText(
                applicationContext,
                getString(R.string.search_add_all_cancelled, mapPositionBGGID.keys.size),
                Toast.LENGTH_SHORT
            )
            toast.show()
        }
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        Log.i("onBackPressed", searchPhrase.toString())
        startActivity(intent)
    }
}