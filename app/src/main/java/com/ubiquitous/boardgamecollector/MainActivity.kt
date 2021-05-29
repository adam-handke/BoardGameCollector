package com.ubiquitous.boardgamecollector

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import java.lang.Exception
import java.util.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    //TODO: sort by order of adding?
    //TODO: table header with column description?
    private var sortBy = R.id.sort_by_name
    private var hideExpansions = true
    private var list: List<BoardGame> = listOf()

    private fun displayBoardGames() {
        //sorting
        list = when (sortBy) {
            R.id.sort_by_year -> list.sortedBy {
                when (it.yearPublished) {
                    null -> Int.MAX_VALUE   //year=null -> sorted as last
                    else -> it.yearPublished
                }
            }
            R.id.sort_by_rank -> list.sortedBy {
                when (it.rank) {
                    0 -> Int.MAX_VALUE  //rank=0 -> sorted as last
                    else -> it.rank
                }
            }
            else -> list.sortedBy {
                it.nameToString("~").toLowerCase(Locale.ROOT) //unnamed -> sorted as last
            }
        }
        val tableLayout: TableLayout = findViewById(R.id.tableLayout)
        tableLayout.removeAllViews()
        for (boardGame in list) {
            Log.i(
                "displayBoardGame",
                "id=${boardGame.id}; name=${boardGame.nameToString(getString(R.string.unnamed_board_game))}"
            )
            if (!(boardGame.baseExpansionStatus == BaseExpansionStatus.EXPANSION && hideExpansions)) {
                val tableRow: View =
                    LayoutInflater.from(this).inflate(R.layout.table_item, null, false)
                val rank: TextView = tableRow.findViewById(R.id.rank)
                val thumbnail: ImageView = tableRow.findViewById(R.id.thumbnail)
                val name: TextView = tableRow.findViewById(R.id.name)

                tableRow.tag = boardGame.id  //store database id as a hidden tag of tableRow
                rank.text = boardGame.rank.toString()
                if (boardGame.thumbnail == null) {
                    thumbnail.setImageBitmap(
                        BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher_foreground)
                    )
                } else {
                    thumbnail.setImageBitmap(
                        boardGame.getThumbnailResizedByWidth(
                            (0.3 * resources.displayMetrics.widthPixels.toFloat()).roundToInt()
                        )
                    )
                }

                name.text = boardGame.nameToString(getString(R.string.unnamed_board_game))

                tableLayout.addView(tableRow)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setLogo(R.mipmap.ic_launcher)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        //supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //supportActionBar?.setHomeAsUpIndicator(R.mipmap.ic_launcher)

        val extras = intent.extras

        //show toast after cancelled adding / successful deleting
        if (intent.hasExtra("id") && extras != null) {
            val msg = when (extras.getInt("id")) {
                0 -> getString(R.string.adding_cancelled)
                else -> getString(R.string.board_game_deleted)
            }
            val toast = Toast.makeText(
                applicationContext,
                msg,
                Toast.LENGTH_SHORT
            )
            toast.show()
        }

        val databaseHandler = DatabaseHandler.getInstance(this)
        //databaseHandler.onUpgrade(databaseHandler.writableDatabase, 1, 1) //reset

        //TODO: async loading games from database
        list = databaseHandler.getAllBoardGamesWithoutDetails()

        databaseHandler.close()
        displayBoardGames()

        //TODO: options menu: locations, artists, designers screens; updating ranks
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main_activity, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        super.onPrepareOptionsMenu(menu)
        if (menu != null) {
            menu.findItem(sortBy).isChecked = true
            menu.findItem(R.id.hideExpansions).isChecked = hideExpansions
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.hideExpansions -> {
                Log.i("onOptionsItemSelected", "hideExpansions=" + item.isChecked.toString())
                item.isChecked = !item.isChecked
                hideExpansions = item.isChecked
                displayBoardGames()
            }
            R.id.add -> {
                val intent = Intent(this, EditActivity::class.java)
                intent.putExtra("id", 0)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                Log.i("goToEditActivity_ADD", "id=0")
                startActivity(intent)
            }
            R.id.addBGG -> {
                val intent = Intent(this, BGGActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                Log.i("goToBGGActivity_ADD", "id=0")
                startActivity(intent)
            }
            R.id.resetCollection -> {
                val databaseHandler = DatabaseHandler.getInstance(this)
                databaseHandler.onUpgrade(databaseHandler.writableDatabase, 1, 1)
                databaseHandler.close()
                list = listOf()
                displayBoardGames()
            }
            R.id.sort_by_name -> {
                sortBy = item.itemId
                displayBoardGames()
            }
            R.id.sort_by_rank -> {
                sortBy = item.itemId
                displayBoardGames()
            }
            R.id.sort_by_year -> {
                sortBy = item.itemId
                displayBoardGames()
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
        return true
    }

    fun goToDetails(v: View) {
        val intent = Intent(this, DetailsActivity::class.java)
        val id = v.tag as Int
        intent.putExtra("id", id)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        Log.i("goToDetailsActivity", "id=$id")
        startActivity(intent)
    }
}