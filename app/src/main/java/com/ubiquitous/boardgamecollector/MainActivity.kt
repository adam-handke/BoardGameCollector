package com.ubiquitous.boardgamecollector

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setLogo(R.mipmap.ic_launcher)
        supportActionBar?.setDisplayUseLogoEnabled(true)

        val databaseHandler = DatabaseHandler(this, null, null, 1)
        databaseHandler.onUpgrade(databaseHandler.writableDatabase, 1, 1) //reset

        databaseHandler.insertBoardGame(BoardGame("Chess"))
        databaseHandler.insertBoardGame(BoardGame("Checkers"))

        val list = databaseHandler.getAllBoardGames()
        for(game in list){
            Log.i("MAIN_ACTIVITY", game.id.toString() + " " + game.name + " " + game.yearPublished)
        }
    }
}