package com.ubiquitous.boardgamecollector

import android.content.Intent
import android.graphics.BitmapFactory
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
import androidx.appcompat.app.AppCompatDelegate
import java.time.LocalDate

class MainActivity : AppCompatActivity() {

    private var sortBy = R.id.sort_by_name
    private var hideExpansions = true
    private lateinit var list: List<BoardGame>

    private fun displayBoardGames() {
        //sorting
        list = when (sortBy) {
            R.id.sort_by_year -> list.sortedBy { it.yearPublished }
            R.id.sort_by_rank -> list.sortedBy { it.rank }
            else -> list.sortedBy { it.name }
        }
        val tableLayout: TableLayout = findViewById(R.id.tableLayout)
        tableLayout.removeAllViews()
        for (boardGame in list) {
            if (!(boardGame.baseExpansionStatus == BaseExpansionStatus.EXPANSION && hideExpansions)) {
                val tableRow: View =
                    LayoutInflater.from(this).inflate(R.layout.table_item, null, false)
                val rank: TextView = tableRow.findViewById(R.id.rank)
                val thumbnail: ImageView = tableRow.findViewById(R.id.thumbnail)
                val name: TextView = tableRow.findViewById(R.id.name)

                tableRow.tag = boardGame.id  //store database id as a hidden tag of tableRow
                rank.text = boardGame.rank.toString()
                thumbnail.setImageBitmap(boardGame.thumbnail)
                name.text =
                    (boardGame.name.toString() + " (" + boardGame.yearPublished.toString() + ")")
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

        val databaseHandler = DatabaseHandler.getInstance(this)
        databaseHandler.onUpgrade(databaseHandler.writableDatabase, 1, 1) //reset

        for (i in 1..2) {
            databaseHandler.insertBoardGame(
                BoardGame(
                    name = "Szachy",
                    originalName = "Chess",
                    yearPublished = LocalDate.now().year + i,
                    description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
                    dateAdded = LocalDate.now().minusDays(50),
                    rrp = "99.99 zł",
                    barcode = "11124040251207",
                    bggid = 111,
                    mpn = "919238",
                    rank = i + i,
                    baseExpansionStatus = BaseExpansionStatus.BOTH,
                    comment = "To moja ulubiona gra!",
                    thumbnail = BitmapFactory.decodeResource(
                        resources,
                        R.mipmap.ic_launcher_foreground
                    ),
                )
            )
            databaseHandler.insertBoardGame(
                BoardGame(
                    name = "Warcaby",
                    originalName = "Checkers",
                    yearPublished = LocalDate.now().year - i,
                    rank = i * i + i,
                    baseExpansionStatus = BaseExpansionStatus.EXPANSION,
                    thumbnail = BitmapFactory.decodeResource(
                        resources,
                        R.mipmap.ic_launcher_foreground
                    )
                )
            )
        }
        databaseHandler.insertArtistOfBoardGame(1, 1234, "John Smith")
        databaseHandler.insertArtistOfBoardGame(1, 1235, "Elizabeth Potter")
        databaseHandler.insertDesignerOfBoardGame(1, 323, "Carl Carlson")
        databaseHandler.insertLocationOfBoardGame(1, "Szafa", "Z prawej strony")
        databaseHandler.insertLocationOfBoardGame(2, "Skrzynia", "Pod książkami")

        list = databaseHandler.getAllBoardGamesWithoutDetails()
        databaseHandler.close()
        displayBoardGames()

        //TODO: blank table row for the case when there are no boardgames in the DB (click = add first)
        //TODO: options menu: add from BGG, add without BGG, BGG screen, locations screen
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
        if (item.itemId == R.id.hideExpansions) {
            Log.i("onOptionsItemSelected", "hideExpansions=" + item.isChecked.toString())
            item.isChecked = !item.isChecked
            hideExpansions = item.isChecked
        } else {
            sortBy = item.itemId
        }
        displayBoardGames()
        return true
    }

    fun goToDetails(v: View) {
        val intent = Intent(this, DetailsActivity::class.java)
        val id = v.tag as Int
        intent.putExtra("id", id)
        Log.i("goToDetails", "id=$id")
        startActivity(intent)
    }
}