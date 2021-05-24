package com.ubiquitous.boardgamecollector

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

class DatabaseHandler(
    context: Context,
    name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    ver: Int
) : SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "BoardGameCollectorDB.db"

        //tables
        const val TABLE_BOARDGAMES = "BoardGames"
        const val TABLE_ARTISTS = "Artists"
        const val TABLE_DESIGNERS = "Designers"
        const val TABLE_LOCATIONS = "Locations"
        const val TABLE_RANK_HISTORY = "RankHistory"
        const val LINK_TABLE_BOARDGAMES_ARTISTS = "BoardGamesArtists"
        const val LINK_TABLE_BOARDGAMES_DESIGNERS = "BoardGamesDesigners"
        const val LINK_TABLE_BOARDGAMES_EXPANSIONS = "BoardGamesExpansions"
        const val LINK_TABLE_BOARDGAMES_LOCATIONS = "BoardGamesLocations"

        //columns
        const val COLUMN_ID = "ID"
        const val COLUMN_NAME = "Name"
        const val COLUMN_ORIGINAL_NAME = "OriginalName"
        const val COLUMN_YEAR_PUBLISHED = "YearPublished"
        const val COLUMN_DESCRIPTION = "Description"
        const val COLUMN_DATE_ORDERED = "DateOrdered"
        const val COLUMN_DATE_ADDED = "DateAdded"
        const val COLUMN_PRICE_PURCHASED = "PricePurchased"
        const val COLUMN_RRP = "RRP"
        const val COLUMN_BARCODE = "Barcode"
        const val COLUMN_BGGID = "BGGID"
        const val COLUMN_MPN = "MPN"
        const val COLUMN_RANK = "Rank"
        const val COLUMN_BASE_EXTENSION_STATUS = "BaseExtensionStatus"
        const val COLUMN_COMMENT = "Comment"
        const val COLUMN_THUMBNAIL = "Thumbnail"
        const val COLUMN_BOARDGAME_ID = "BoardGameID"
        const val COLUMN_ARTIST_ID = "ArtistID"
        const val COLUMN_DESIGNER_ID = "DesignerID"
        const val COLUMN_EXPANSION_BGGID = "ExpansionBGGID"
        const val COLUMN_EXPANSION_NAME = "ExpansionName"
        const val COLUMN_LOCATION_ID = "LocationID"
        const val COLUMN_DATE_RETRIEVED = "DateRetrieved"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(
            "CREATE TABLE $TABLE_BOARDGAMES (\n" +
                    "    $COLUMN_ID                     INTEGER PRIMARY KEY,\n" +
                    "    $COLUMN_NAME                   TEXT,\n" +
                    "    $COLUMN_ORIGINAL_NAME          TEXT,\n" +
                    "    $COLUMN_YEAR_PUBLISHED         INTEGER,\n" +
                    "    $COLUMN_DESCRIPTION            TEXT,\n" +
                    "    $COLUMN_DATE_ORDERED           DATE,\n" +
                    "    $COLUMN_DATE_ADDED             DATE,\n" +
                    "    $COLUMN_PRICE_PURCHASED        TEXT,\n" +
                    "    $COLUMN_RRP                    TEXT,\n" +
                    "    $COLUMN_BARCODE                TEXT,\n" +
                    "    $COLUMN_BGGID                  INTEGER DEFAULT (0),\n" +
                    "    $COLUMN_MPN                    TEXT,\n" +
                    "    $COLUMN_RANK                   INTEGER DEFAULT (0),\n" +
                    "    $COLUMN_BASE_EXTENSION_STATUS  TEXT " +
                    "       CHECK ($COLUMN_BASE_EXTENSION_STATUS IN " +
                    "       ('${BaseExtensionStatus.BASE.name}', " +
                    "       '${BaseExtensionStatus.EXTENSION.name}', " +
                    "       '${BaseExtensionStatus.BOTH.name}') ) \n" +
                    "       DEFAULT ${BaseExtensionStatus.BASE.name},\n" +
                    "    $COLUMN_COMMENT                TEXT,\n" +
                    "    $COLUMN_THUMBNAIL              BLOB\n" +
                    ");"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_BOARDGAMES")
        onCreate(db)
    }

    //TODO: parameterized ORDER BY
    fun getAllBoardGamesWithoutDetails(): List<BoardGame> {
        val list: MutableList<BoardGame> = mutableListOf()
        val query =
            "SELECT $COLUMN_ID, $COLUMN_NAME, $COLUMN_YEAR_PUBLISHED, $COLUMN_DESCRIPTION, $COLUMN_DATE_ADDED, " +
                    "$COLUMN_BGGID, $COLUMN_RANK, $COLUMN_BASE_EXTENSION_STATUS, $COLUMN_THUMBNAIL " +
                    "FROM $TABLE_BOARDGAMES;"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        try {
            while (cursor.moveToNext()) {
                val boardGame = BoardGame()
                boardGame.id = cursor.getInt(0)
                boardGame.name = cursor.getString(1)
                boardGame.yearPublished = cursor.getInt(2)
                boardGame.description = cursor.getString(3)
                boardGame.dateAdded =
                    Instant.ofEpochMilli(cursor.getLong(4)).atZone(ZoneId.systemDefault())
                        .toLocalDate()
                boardGame.bggid = cursor.getInt(5)
                boardGame.rank = cursor.getInt(6)
                when (cursor.getString(7)) {
                    BaseExtensionStatus.EXTENSION.name -> boardGame.baseExtensionStatus =
                        BaseExtensionStatus.EXTENSION
                    BaseExtensionStatus.BOTH.name -> boardGame.baseExtensionStatus =
                        BaseExtensionStatus.BOTH
                    else -> {
                        boardGame.baseExtensionStatus = BaseExtensionStatus.BASE
                    }
                }
                //TODO:thumbnail
                val tmpThumbnail = cursor.getBlob(8)
                boardGame.thumbnail =
                    BitmapFactory.decodeByteArray(tmpThumbnail, 0, tmpThumbnail.size)
                list.add(boardGame)
            }
        } catch (e: Exception) {
            Log.e("getAllBoardGames_EXCEPTION", e.message.toString())
        } finally {
            cursor.close()
        }
        db.close()
        return list.sortedBy { it.name }
    }

    fun getBoardGameByID(id: Int): BoardGame {
        val boardGame = BoardGame()
        val query = "SELECT * FROM $TABLE_BOARDGAMES WHERE $COLUMN_ID = $id;"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        try {
            if (cursor.moveToFirst()) {
                boardGame.id = cursor.getInt(0)
                boardGame.name = cursor.getString(1)
                boardGame.originalName = cursor.getString(2)
                boardGame.yearPublished = cursor.getInt(3)
                boardGame.description = cursor.getString(4)
                boardGame.dateOrdered =
                    Instant.ofEpochMilli(cursor.getLong(5)).atZone(ZoneId.systemDefault())
                        .toLocalDate()
                boardGame.dateAdded =
                    Instant.ofEpochMilli(cursor.getLong(6)).atZone(ZoneId.systemDefault())
                        .toLocalDate()
                boardGame.pricePurchased = cursor.getString(7)
                boardGame.rrp = cursor.getString(8)
                boardGame.barcode = cursor.getString(9)
                boardGame.bggid = cursor.getInt(10)
                boardGame.mpn = cursor.getString(11)
                boardGame.rank = cursor.getInt(12)
                when (cursor.getString(13)) {
                    BaseExtensionStatus.EXTENSION.name -> boardGame.baseExtensionStatus =
                        BaseExtensionStatus.EXTENSION
                    BaseExtensionStatus.BOTH.name -> boardGame.baseExtensionStatus =
                        BaseExtensionStatus.BOTH
                    else -> {
                        boardGame.baseExtensionStatus = BaseExtensionStatus.BASE
                    }
                }
                boardGame.comment = cursor.getString(14)
                //TODO:thumbnail
                val tmpThumbnail = cursor.getBlob(15)
                boardGame.thumbnail =
                    BitmapFactory.decodeByteArray(tmpThumbnail, 0, tmpThumbnail.size)
            }
        } catch (e: Exception) {
            Log.e("getBoardGameByID_EXCEPTION", e.message.toString())
        } finally {
            cursor.close()
        }
        db.close()
        return boardGame
    }

    fun insertBoardGame(boardGame: BoardGame) {
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(COLUMN_ID, boardGame.id)
        values.put(COLUMN_NAME, boardGame.name)
        values.put(COLUMN_ORIGINAL_NAME, boardGame.originalName)
        values.put(COLUMN_YEAR_PUBLISHED, boardGame.yearPublished)
        values.put(COLUMN_DESCRIPTION, boardGame.description)
        values.put(
            COLUMN_DATE_ORDERED, boardGame.dateOrdered?.atTime(LocalTime.MIDNIGHT)?.atZone(
                ZoneId.systemDefault()
            )?.toInstant()?.toEpochMilli()
        )
        values.put(
            COLUMN_DATE_ADDED, boardGame.dateAdded?.atTime(LocalTime.MIDNIGHT)?.atZone(
                ZoneId.systemDefault()
            )?.toInstant()?.toEpochMilli()
        )
        values.put(COLUMN_PRICE_PURCHASED, boardGame.pricePurchased)
        values.put(COLUMN_RRP, boardGame.rrp)
        values.put(COLUMN_BARCODE, boardGame.barcode)
        values.put(COLUMN_BGGID, boardGame.bggid)
        values.put(COLUMN_MPN, boardGame.mpn)
        values.put(COLUMN_RANK, boardGame.rank)
        values.put(COLUMN_BASE_EXTENSION_STATUS, boardGame.baseExtensionStatus.name)
        values.put(COLUMN_COMMENT, boardGame.comment)
        val byteArrayOutputStream = ByteArrayOutputStream()
        boardGame.thumbnail?.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        values.put(COLUMN_THUMBNAIL, byteArrayOutputStream.toByteArray())

        db.insert(TABLE_BOARDGAMES, null, values)
        db.close()
    }
}