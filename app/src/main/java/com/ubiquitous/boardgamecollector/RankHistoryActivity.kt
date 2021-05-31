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
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.lang.Exception
import java.time.LocalDate
import java.time.format.DateTimeFormatter

//TODO: update ranking of this game only
class RankHistoryActivity : AppCompatActivity() {

    private lateinit var rankHistoryListView: ListView
    private var apiAsyncTask: RankHistoryActivity.APIAsyncTask = APIAsyncTask()
    private var id = 0
    private var bggid = 0
    private var name = ""
    private var list: List<Pair<Int, LocalDate>> = listOf()
    private val pattern = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    private val context = this

    private fun displayRankHistory() {
        val databaseHandler = DatabaseHandler.getInstance(this)
        list = databaseHandler.getRankHistory(id)
        databaseHandler.close()

        val rows = ArrayList<HashMap<String, String>>()
        if (list.isNotEmpty()) {
            for (pair in list) {
                val item = HashMap<String, String>()
                item["date"] = pair.second.format(pattern)
                item["rank"] = pair.first.toString()
                rows.add(item)
            }
        } else {
            val item = HashMap<String, String>()
            item["date"] = getString(R.string.rank_history_empty)
            item["rank"] = ""
            rows.add(item)
        }


        val adapter = SimpleAdapter(
            this,
            rows,
            android.R.layout.simple_list_item_2,
            arrayOf("date", "rank"),
            intArrayOf(android.R.id.text1, android.R.id.text2)
        )

        rankHistoryListView.adapter = adapter
    }

    //async task for loading rank (by game bggid)
    inner class APIAsyncTask : AsyncTask<Int, Int, Boolean>() {
        var isRunning = false

        override fun doInBackground(vararg params: Int?): Boolean {
            isRunning = true
            val bggid = params[0]
            if(bggid != null) {
                return try {
                    val boardGame = BoardGameGeek().loadBoardGame(bggid)
                    val databaseHandler = DatabaseHandler.getInstance(context)
                    databaseHandler.insertRankHistoryRecord(id, boardGame.rank)
                    databaseHandler.close()
                    true
                } catch (e: Exception) {
                    Log.e(
                        "doInBackground_EXCEPTION",
                        "bggid=$bggid; ${e.message}; ${e.stackTraceToString()}"
                    )
                    false
                }
            } else {
                return false
            }
        }

        override fun onCancelled() {
            super.onCancelled()
            if (isRunning) {
                val toast = Toast.makeText(
                    applicationContext,
                    getString(R.string.rank_update_cancelled),
                    Toast.LENGTH_SHORT
                )
                toast.show()
                //isRunning = false
            }
        }

        override fun onPostExecute(result: Boolean) {
            super.onPostExecute(result)
            isRunning = false
            if(result){
                val toast = Toast.makeText(
                    applicationContext,
                    getString(R.string.rank_updated),
                    Toast.LENGTH_SHORT
                )
                toast.show()
                displayRankHistory()
            } else {
                val toast = Toast.makeText(
                    applicationContext,
                    getString(R.string.rank_not_updated),
                    Toast.LENGTH_SHORT
                )
                toast.show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rank_history)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val extras = intent.extras ?: return
        id = extras.getInt("id")
        bggid = extras.getInt("bggid")
        name = extras.getString("name").toString()

        supportActionBar?.setTitle(R.string.rank_history_title)
        supportActionBar?.subtitle = name

        rankHistoryListView = findViewById(R.id.rankHistoryList)

        //insert an empty footer in order not to cover the last row with the button
        val emptyTextView = TextView(this)
        emptyTextView.height = (resources.displayMetrics.density * 64 + 0.5).toInt()
        emptyTextView.isClickable = false
        rankHistoryListView.addFooterView(emptyTextView, null, false)

        displayRankHistory()
    }

    fun updateRank(view: View) {
        apiAsyncTask.cancel(true)
        apiAsyncTask = APIAsyncTask()
        if(bggid <= 0){
            val toast = Toast.makeText(
                applicationContext,
                getString(R.string.update_rank_wrong_bggid),
                Toast.LENGTH_SHORT
            )
            toast.show()
        } else {
            apiAsyncTask.execute(bggid)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
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
        apiAsyncTask.cancel(true)
        super.onBackPressed()
        val intent = Intent(this, DetailsActivity::class.java)
        intent.putExtra("id", id)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        Log.i("onBackPressed", "id=$id")
        startActivity(intent)
    }
}