package com.ubiquitous.boardgamecollector

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.ListView
import android.widget.SimpleAdapter
import androidx.appcompat.app.AppCompatActivity

class DetailsActivity : AppCompatActivity() {

    private lateinit var detailListView: ListView
    private var id: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        //supportActionBar?.setDisplayShowHomeEnabled(true)
        //supportActionBar?.setLogo(R.mipmap.ic_launcher)
        //supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //supportActionBar?.setHomeAsUpIndicator(R.mipmap.ic_launcher)
        supportActionBar?.setTitle(R.string.details)

        val extras = intent.extras ?: return
        id = extras.getInt("id")
        val databaseHandler = DatabaseHandler.getInstance(this)

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
            getString(R.string.comment),
            getString(R.string.location),
            getString(R.string.location_comment)
        )
        val boardGame = databaseHandler.getBoardGameByID(id)
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
        imageView.setImageBitmap(boardGame.thumbnail)
        detailListView.addHeaderView(imageView)

        databaseHandler.close()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_details_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                /*
                val intent = Intent(this, MainActivity::class.java)
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                Log.i("goToMainActivity", "id=$id")
                startActivity(intent)
                 */
                onBackPressed()
                true
            }
            R.id.edit -> {
                val intent = Intent(this, EditActivity::class.java)
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra("id", id)
                Log.i("goToEditActivity", "id=$id")
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        Log.i("onBackPressed", "id=$id")
        startActivity(intent)
    }

    //TODO: on click actions specific for every row
}