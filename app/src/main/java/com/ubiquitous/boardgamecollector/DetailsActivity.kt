package com.ubiquitous.boardgamecollector

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class DetailsActivity : AppCompatActivity() {

    private lateinit var detailListView: ListView
    private var boardGameID = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        //supportActionBar?.setDisplayShowHomeEnabled(true)
        //supportActionBar?.setLogo(R.mipmap.ic_launcher)
        //supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //supportActionBar?.setHomeAsUpIndicator(R.mipmap.ic_launcher)
        supportActionBar?.setTitle(R.string.details_title)

        val extras = intent.extras ?: return
        boardGameID = extras.getInt("id")

        //show toast after editing details / adding board game
        if (intent.hasExtra("edit")) {
            val msg: String
            val edit = extras.getBoolean("edit")
            val add = extras.getBoolean("add")
            msg = if (edit && add) {
                //game was successfully added
                getString(R.string.board_game_added)
            } else {
                if (edit) {
                    //game was successfully edited
                    getString(R.string.board_game_edited)
                } else {
                    //editing of the game was cancelled
                    getString(R.string.editing_cancelled)
                }
            }
            val toast = Toast.makeText(
                applicationContext,
                msg,
                Toast.LENGTH_SHORT
            )
            toast.show()
        }

        val databaseHandler = DatabaseHandler.getInstance(this)
        val boardGame = databaseHandler.getBoardGameDetails(boardGameID)
        databaseHandler.close()

        supportActionBar?.subtitle = boardGame.nameToString(getString(R.string.unnamed_board_game))

        //TODO: expansion names
        val detailNames = arrayOf(
            getString(R.string.name),
            getString(R.string.original_name),
            getString(R.string.year_published),
            getString(R.string.designers),
            getString(R.string.artists),
            getString(R.string.description),
            getString(R.string.date_ordered),
            getString(R.string.date_added),
            getString(R.string.price_purchased),
            getString(R.string.rrp),
            getString(R.string.barcode),
            getString(R.string.bggid),
            getString(R.string.mpn),
            getString(R.string.rank),
            getString(R.string.base_expansion_status),
            getString(R.string.expansions),
            getString(R.string.comment),
            getString(R.string.location),
            getString(R.string.location_comment)
        )
        val detailValues = boardGame.toStringArray(
            getString(R.string.base),
            getString(R.string.expansion),
            getString(R.string.both)
        )

        val details = ArrayList<HashMap<String, String>>()
        for (i in detailNames.indices) {
            val item = HashMap<String, String>()
            item["name"] = detailNames[i]
            item["value"] = detailValues[i]
            details.add(item)
        }

        val adapter = SimpleAdapter(
            this,
            details,
            android.R.layout.simple_list_item_2,
            arrayOf("name", "value"),
            intArrayOf(android.R.id.text1, android.R.id.text2)
        )

        detailListView = findViewById(R.id.detailList)
        detailListView.adapter = adapter

        //thumbnail as listview header
        val imageView = ImageView(this)
        if (boardGame.thumbnail == null) {
            imageView.setImageBitmap(
                BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher_foreground)
            )
        } else {
            imageView.setImageBitmap(boardGame.getThumbnailResizedByWidth(resources.displayMetrics.widthPixels))
        }
        detailListView.addHeaderView(imageView)

        /*TODO: on click actions specific for every row:
            designers - go to designers activity???
            artists - go to artists activity???
            BGGID - open URL with that ID through a web browser
            location - locations activity
                other rows - copy to clipboard?
         */
        detailListView.setOnItemClickListener { _, _, position, _ ->
            Log.i("setOnItemClickListener_Clipboard", "position=$position")
            if (position == 14) {
                //go to rank history
                val intent = Intent(this, RankHistoryActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra("id", boardGameID)
                intent.putExtra("name", boardGame.nameToString(getString(R.string.unnamed_board_game)))
                Log.i("goToRankHistoryActivity", "id=$boardGameID")
                startActivity(intent)
            }/*else if (position != 0) {
                //copy to clipboard
                Log.i("setOnItemClickListener_Clipboard", "position=$position")
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

                @Suppress("UNCHECKED_CAST")
                val element = adapter.getItem(position) as HashMap<String, String>
                val clip = ClipData.newPlainText(element["name"], element["value"])
                clipboard.setPrimaryClip(clip)

                val toast = Toast.makeText(
                    applicationContext,
                    getString(R.string.clipboard),
                    Toast.LENGTH_SHORT
                )
                toast.show()
            }
            */
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_details_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
            R.id.edit -> {
                val intent = Intent(this, EditActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra("id", boardGameID)
                Log.i("goToEditActivity", "id=$boardGameID")
                startActivity(intent)
            }
            R.id.delete -> {
                val databaseHandler = DatabaseHandler.getInstance(this)
                databaseHandler.deleteBoardGame(boardGameID)

                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra("id", boardGameID)
                Log.i("goToMainActivity_DELETE", "id=$boardGameID")
                startActivity(intent)
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
        Log.i("onBackPressed", "id=$boardGameID")
        startActivity(intent)
    }
}