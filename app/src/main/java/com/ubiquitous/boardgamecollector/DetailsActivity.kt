package com.ubiquitous.boardgamecollector

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.ListView
import android.widget.SimpleAdapter
import java.time.LocalDate

class DetailsActivity : AppCompatActivity() {

    private lateinit var detailListView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setLogo(R.mipmap.ic_launcher)
        supportActionBar?.setDisplayUseLogoEnabled(true)

        val extras = intent.extras ?: return
        val id = extras.getInt("id")
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

    //TODO: on click actions specific for every row
}