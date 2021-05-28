package com.ubiquitous.boardgamecollector

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import java.lang.Exception

class BGGActivity : AppCompatActivity() {
    private var searchPhrase: String? = null
    private var list: List<BoardGame> = listOf()

    private fun displaySearchResults() {
        //TODO: displaying search results in a list view
    }

    private inner class APIAsyncTask : AsyncTask<String, Int, String>() {
        override fun doInBackground(vararg params: String?): String? {
            return try {
                //list = BoardGameGeek().searchBoardGamesByUsername("matt")
                list = listOf(
                    BoardGameGeek().loadBoardGame(102794)
                )
                "true"
            } catch (e: Exception) {
                list = listOf()
                "false"
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            displaySearchResults()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bgg)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.add_bgg_title)

        //TODO
    }

    //TODO:
    fun searchName(view: View) {

    }

    //TODO:
    fun searchUsername(view: View) {

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
            else -> {
            }
        }
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        Log.i("onBackPressed", searchPhrase.toString())
        startActivity(intent)
    }
}