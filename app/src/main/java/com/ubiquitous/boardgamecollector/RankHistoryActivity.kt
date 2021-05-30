package com.ubiquitous.boardgamecollector

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import android.widget.SimpleAdapter
import java.time.format.DateTimeFormatter

//TODO: update ranking of this game only
class RankHistoryActivity : AppCompatActivity() {

    private lateinit var rankHistoryListView: ListView
    private var id = 0
    private var name = ""
    private val pattern = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rank_history)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //TODO: details - extras!
        val extras = intent.extras ?: return
        id = extras.getInt("id")
        name = extras.getString("name").toString()

        supportActionBar?.setTitle(R.string.rank_history_title)
        supportActionBar?.subtitle = name

        val databaseHandler = DatabaseHandler.getInstance(this)
        val list = databaseHandler.getRankHistory(id)
        databaseHandler.close()

        val rows = ArrayList<HashMap<String, String>>()
        if(list.isNotEmpty()){
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

        rankHistoryListView = findViewById(R.id.rankHistoryList)
        rankHistoryListView.adapter = adapter

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
        super.onBackPressed()
        val intent = Intent(this, DetailsActivity::class.java)
        intent.putExtra("id", id)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        Log.i("onBackPressed", "id=$id")
        startActivity(intent)
    }
}