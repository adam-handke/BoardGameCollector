package com.ubiquitous.boardgamecollector

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import java.time.LocalDate

class MainActivity : AppCompatActivity() {

    private val databaseHandler = DatabaseHandler.getInstance(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setLogo(R.mipmap.ic_launcher)
        supportActionBar?.setDisplayUseLogoEnabled(true)

        databaseHandler.onUpgrade(databaseHandler.writableDatabase, 1, 1) //reset

        for (i in 1..2) {
            databaseHandler.insertBoardGame(
                BoardGame(
                    name = "Szachy",
                    originalName = "Chess",
                    yearPublished = LocalDate.now().year,
                    description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
                    dateAdded = LocalDate.now(),
                    rrp = "99.99 zł",
                    barcode = "11124040251207",
                    bggid = 111,
                    mpn = "919238",
                    rank = i,
                    baseExpansionStatus = BaseExpansionStatus.BASE,
                    comment = "To moja ulubiona gra!",
                    thumbnail = BitmapFactory.decodeResource(
                        resources,
                        R.mipmap.ic_launcher_foreground
                    ),
                )
            )
            databaseHandler.insertBoardGame(
                BoardGame(
                    name = "Checkers",
                    yearPublished = LocalDate.now().year,
                    rank = i * i,
                    thumbnail = BitmapFactory.decodeResource(
                        resources,
                        R.mipmap.ic_launcher_foreground
                    )
                )
            )
        }

        val tableLayout: TableLayout = findViewById(R.id.tableLayout)
        val list = databaseHandler.getAllBoardGamesWithoutDetails()
        for (game in list) {
            if (game.baseExpansionStatus != BaseExpansionStatus.EXPANSION) {
                val tableRow: View =
                    LayoutInflater.from(this).inflate(R.layout.table_item, null, false)
                val rank: TextView = tableRow.findViewById(R.id.rank)
                val thumbnail: ImageView = tableRow.findViewById(R.id.thumbnail)
                val name: TextView = tableRow.findViewById(R.id.name)

                tableRow.tag = game.id  //store database id as a hidden tag of tableRow
                rank.text = game.rank.toString()
                thumbnail.setImageBitmap(game.thumbnail)
                name.text = (game.name.toString() + " (" + game.yearPublished.toString() + ")")
                tableLayout.addView(tableRow)
            }
        }

        //TODO: blank table row for the case when there are no boardgames in the DB (click = add first)
        //TODO: options menu: add, delete, sort, BGG screen, locations screen
    }

    fun goToDetails(v: View) {
        databaseHandler.close()
        val intent = Intent(this, DetailsActivity::class.java)
        val id = v.tag as Int
        intent.putExtra("id", id)
        Log.i("goToDetails", "id=$id")
        startActivity(intent)
    }
}