package com.ubiquitous.boardgamecollector

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import java.lang.Exception

class BGGActivity : AppCompatActivity() {
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

        //TODO
    }
}