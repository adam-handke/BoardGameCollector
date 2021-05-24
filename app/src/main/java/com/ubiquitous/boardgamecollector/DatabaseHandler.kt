package com.ubiquitous.boardgamecollector

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.database.getBlobOrNull
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
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
        const val COLUMN_BASE_EXPANSION_STATUS = "BaseExtensionStatus"
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
                    "    $COLUMN_BASE_EXPANSION_STATUS  TEXT " +
                    "       CHECK ($COLUMN_BASE_EXPANSION_STATUS IN " +
                    "       ('${BaseExpansionStatus.BASE.name}', " +
                    "       '${BaseExpansionStatus.EXPANSION.name}', " +
                    "       '${BaseExpansionStatus.BOTH.name}') ) \n" +
                    "       DEFAULT ${BaseExpansionStatus.BASE.name},\n" +
                    "    $COLUMN_COMMENT                TEXT,\n" +
                    "    $COLUMN_THUMBNAIL              BLOB\n" +
                    ");"
        )
        db?.execSQL(
            "CREATE TABLE $TABLE_ARTISTS (\n" +
                    "    $COLUMN_ID                     INTEGER PRIMARY KEY,\n" +
                    "    $COLUMN_BGGID                  INTEGER DEFAULT (0),\n" +
                    "    $COLUMN_NAME                   TEXT\n" +
                    ");"
        )
        db?.execSQL(
            "CREATE TABLE $LINK_TABLE_BOARDGAMES_ARTISTS (\n" +
                    "    $COLUMN_BOARDGAME_ID INTEGER REFERENCES $TABLE_BOARDGAMES ($COLUMN_ID),\n" +
                    "    $COLUMN_ARTIST_ID    INTEGER REFERENCES $TABLE_ARTISTS ($COLUMN_ID),\n" +
                    "                         PRIMARY KEY (\n" +
                    "                           $COLUMN_BOARDGAME_ID,\n" +
                    "                           $COLUMN_ARTIST_ID\n" +
                    "    )\n" +
                    ");"
        )
        db?.execSQL(
            "CREATE TABLE $TABLE_DESIGNERS (\n" +
                    "    $COLUMN_ID                     INTEGER PRIMARY KEY,\n" +
                    "    $COLUMN_BGGID                  INTEGER DEFAULT (0),\n" +
                    "    $COLUMN_NAME                   TEXT\n" +
                    ");"
        )
        db?.execSQL(
            "CREATE TABLE $LINK_TABLE_BOARDGAMES_DESIGNERS (\n" +
                    "    $COLUMN_BOARDGAME_ID   INTEGER REFERENCES $TABLE_BOARDGAMES ($COLUMN_ID),\n" +
                    "    $COLUMN_DESIGNER_ID    INTEGER REFERENCES $TABLE_DESIGNERS ($COLUMN_ID),\n" +
                    "                           PRIMARY KEY (\n" +
                    "                               $COLUMN_BOARDGAME_ID,\n" +
                    "                               $COLUMN_DESIGNER_ID\n" +
                    "    )\n" +
                    ");"
        )
        db?.execSQL(
            "CREATE TABLE $TABLE_LOCATIONS (\n" +
                    "    $COLUMN_ID             INTEGER PRIMARY KEY,\n" +
                    "    $COLUMN_NAME           TEXT\n" +
                    ");"
        )
        db?.execSQL(
            "CREATE TABLE $LINK_TABLE_BOARDGAMES_LOCATIONS (\n" +
                    "    $COLUMN_BOARDGAME_ID   INTEGER REFERENCES $TABLE_BOARDGAMES ($COLUMN_ID) \n" +
                    "                           UNIQUE,\n" +
                    "    $COLUMN_LOCATION_ID    INTEGER REFERENCES $TABLE_LOCATIONS ($COLUMN_ID),\n" +
                    "    $COLUMN_COMMENT        TEXT,\n" +
                    "                           PRIMARY KEY (\n" +
                    "                               $COLUMN_BOARDGAME_ID,\n" +
                    "                               $COLUMN_LOCATION_ID\n" +
                    "    )\n" +
                    ");"
        )
        db?.execSQL(
            "CREATE TABLE $LINK_TABLE_BOARDGAMES_EXPANSIONS (\n" +
                    "    $COLUMN_BOARDGAME_ID    INTEGER REFERENCES $TABLE_BOARDGAMES ($COLUMN_ID),\n" +
                    "    $COLUMN_EXPANSION_BGGID INTEGER,\n" +
                    "    $COLUMN_EXPANSION_NAME  TEXT,\n" +
                    "    PRIMARY KEY (\n" +
                    "        $COLUMN_BOARDGAME_ID,\n" +
                    "        $COLUMN_EXPANSION_BGGID\n" +
                    "    )\n" +
                    ");"
        )
        db?.execSQL(
            "CREATE TABLE $TABLE_RANK_HISTORY (\n" +
                    "    $COLUMN_ID             INTEGER PRIMARY KEY,\n" +
                    "    $COLUMN_BOARDGAME_ID   INTEGER REFERENCES $TABLE_BOARDGAMES ($COLUMN_ID) \n" +
                    "                           NOT NULL,\n" +
                    "    $COLUMN_RANK           INTEGER NOT NULL\n" +
                    "                           DEFAULT (0),\n" +
                    "    $COLUMN_DATE_RETRIEVED DATE    NOT NULL\n" +
                    ");"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_BOARDGAMES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_ARTISTS")
        db?.execSQL("DROP TABLE IF EXISTS $LINK_TABLE_BOARDGAMES_ARTISTS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_DESIGNERS")
        db?.execSQL("DROP TABLE IF EXISTS $LINK_TABLE_BOARDGAMES_DESIGNERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_LOCATIONS")
        db?.execSQL("DROP TABLE IF EXISTS $LINK_TABLE_BOARDGAMES_LOCATIONS")
        db?.execSQL("DROP TABLE IF EXISTS $LINK_TABLE_BOARDGAMES_EXPANSIONS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_RANK_HISTORY")
        onCreate(db)
    }

    //TODO: parameterized ORDER BY
    fun getAllBoardGamesWithoutDetails(): List<BoardGame> {
        val list: MutableList<BoardGame> = mutableListOf()
        val query =
            "SELECT $COLUMN_ID, $COLUMN_NAME, $COLUMN_YEAR_PUBLISHED, $COLUMN_DESCRIPTION, $COLUMN_DATE_ADDED, " +
                    "$COLUMN_BGGID, $COLUMN_RANK, $COLUMN_BASE_EXPANSION_STATUS, $COLUMN_THUMBNAIL " +
                    "FROM $TABLE_BOARDGAMES;"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        try {
            while (cursor.moveToNext()) {
                val boardGame = BoardGame()
                boardGame.id = cursor.getIntOrNull(0)
                boardGame.name = cursor.getStringOrNull(1)
                boardGame.yearPublished = cursor.getIntOrNull(2)
                boardGame.description = cursor.getStringOrNull(3)
                boardGame.dateAdded =
                    cursor.getLongOrNull(4)?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                boardGame.bggid = cursor.getInt(5)
                boardGame.rank = cursor.getInt(6)
                when (cursor.getString(7)) {
                    BaseExpansionStatus.EXPANSION.name -> boardGame.baseExpansionStatus =
                        BaseExpansionStatus.EXPANSION
                    BaseExpansionStatus.BOTH.name -> boardGame.baseExpansionStatus =
                        BaseExpansionStatus.BOTH
                    else -> {
                        boardGame.baseExpansionStatus = BaseExpansionStatus.BASE
                    }
                }
                //TODO:thumbnail
                val tmpThumbnail = cursor.getBlobOrNull(8)
                if (tmpThumbnail != null) {
                    boardGame.thumbnail =
                        BitmapFactory.decodeByteArray(tmpThumbnail, 0, tmpThumbnail.size)
                }
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

    private fun getArtistNamesByBoardGameID(id: Int): List<String> {
        val list = mutableListOf<String>()
        if (id > 0) {
            val query = "SELECT $TABLE_ARTISTS.$COLUMN_NAME " +
                    "FROM $LINK_TABLE_BOARDGAMES_ARTISTS INNER JOIN $TABLE_ARTISTS " +
                    "ON $LINK_TABLE_BOARDGAMES_ARTISTS.$COLUMN_ARTIST_ID = $TABLE_ARTISTS.$COLUMN_ID " +
                    "WHERE $LINK_TABLE_BOARDGAMES_ARTISTS.$COLUMN_BOARDGAME_ID = $id " +
                    "ORDER BY $TABLE_ARTISTS.$COLUMN_NAME"
            val db = this.writableDatabase
            val cursor = db.rawQuery(query, null)
            try {
                while (cursor.moveToNext()) {
                    list.add(cursor.getString(0))
                }
            } catch (e: Exception) {
                Log.e("getArtistsByBoardGameID_EXCEPTION", e.message.toString())
            } finally {
                cursor.close()
            }
            db.close()
        }
        return list
    }

    private fun getDesignerNamesByBoardGameID(id: Int): List<String> {
        val list = mutableListOf<String>()
        if (id > 0) {
            val query = "SELECT $TABLE_DESIGNERS.$COLUMN_NAME " +
                    "FROM $LINK_TABLE_BOARDGAMES_DESIGNERS INNER JOIN $TABLE_DESIGNERS " +
                    "ON $LINK_TABLE_BOARDGAMES_DESIGNERS.$COLUMN_DESIGNER_ID = $TABLE_DESIGNERS.$COLUMN_ID " +
                    "WHERE $LINK_TABLE_BOARDGAMES_DESIGNERS.$COLUMN_BOARDGAME_ID = $id " +
                    "ORDER BY $TABLE_DESIGNERS.$COLUMN_NAME"
            val db = this.writableDatabase
            val cursor = db.rawQuery(query, null)
            try {
                while (cursor.moveToNext()) {
                    list.add(cursor.getString(0))
                }
            } catch (e: Exception) {
                Log.e("getDesignerNamesByBoardGameID_EXCEPTION", e.message.toString())
            } finally {
                cursor.close()
            }
            db.close()
        }
        return list
    }

    private fun getLocationNameAndCommentByBoardGameID(id: Int): Pair<String?, String?> {
        //first - location name
        //second - comment on location
        var pair = Pair<String?, String?>(null, null)
        if (id > 0) {
            val query = "SELECT $TABLE_LOCATIONS.$COLUMN_NAME, $LINK_TABLE_BOARDGAMES_LOCATIONS.$COLUMN_COMMENT " +
                    "FROM $LINK_TABLE_BOARDGAMES_LOCATIONS INNER JOIN $TABLE_LOCATIONS " +
                    "ON $LINK_TABLE_BOARDGAMES_LOCATIONS.$COLUMN_LOCATION_ID = $TABLE_LOCATIONS.$COLUMN_ID " +
                    "WHERE $LINK_TABLE_BOARDGAMES_LOCATIONS.$COLUMN_BOARDGAME_ID = $id " +
                    "ORDER BY $TABLE_LOCATIONS.$COLUMN_NAME"
            val db = this.writableDatabase
            val cursor = db.rawQuery(query, null)
            try {
                while (cursor.moveToNext()) {
                    pair = Pair(cursor.getStringOrNull(0), cursor.getStringOrNull(1))
                }
            } catch (e: Exception) {
                Log.e("getLocationNameAndCommentByBoardGameID_EXCEPTION", e.message.toString())
            } finally {
                cursor.close()
            }
            db.close()
        }
        return pair
    }

    private fun getExpansionNamesByBoardGameID(id: Int): List<String>{
        val list = mutableListOf<String>()
        if (id > 0) {
            val query = "SELECT $COLUMN_EXPANSION_NAME FROM $LINK_TABLE_BOARDGAMES_EXPANSIONS " +
                    "WHERE $COLUMN_BOARDGAME_ID = $id ORDER BY $COLUMN_EXPANSION_NAME"
            val db = this.writableDatabase
            val cursor = db.rawQuery(query, null)
            try {
                while (cursor.moveToNext()) {
                    list.add(cursor.getString(0))
                }
            } catch (e: Exception) {
                Log.e("getExpansionNamesByBoardGameID_EXCEPTION", e.message.toString())
            } finally {
                cursor.close()
            }
            db.close()
        }
        return list
    }

    fun getBoardGameByID(id: Int): BoardGame {
        val boardGame = BoardGame()
        val query = "SELECT * FROM $TABLE_BOARDGAMES WHERE $COLUMN_ID = $id;"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        try {
            if (cursor.moveToFirst()) {
                boardGame.id = cursor.getInt(0)
                boardGame.name = cursor.getStringOrNull(1)
                boardGame.originalName = cursor.getStringOrNull(2)
                boardGame.yearPublished = cursor.getInt(3)
                boardGame.designers = getDesignerNamesByBoardGameID(boardGame.id!!)
                boardGame.artists = getArtistNamesByBoardGameID(boardGame.id!!)
                boardGame.description = cursor.getStringOrNull(4)
                boardGame.dateOrdered =
                    cursor.getLongOrNull(5)?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                boardGame.dateAdded =
                    cursor.getLongOrNull(6)?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                boardGame.pricePurchased = cursor.getStringOrNull(7)
                boardGame.rrp = cursor.getStringOrNull(8)
                boardGame.barcode = cursor.getStringOrNull(9)
                boardGame.bggid = cursor.getInt(10)
                boardGame.mpn = cursor.getStringOrNull(11)
                boardGame.rank = cursor.getInt(12)
                when (cursor.getString(13)) {
                    BaseExpansionStatus.EXPANSION.name -> boardGame.baseExpansionStatus =
                        BaseExpansionStatus.EXPANSION
                    BaseExpansionStatus.BOTH.name -> boardGame.baseExpansionStatus =
                        BaseExpansionStatus.BOTH
                    else -> {
                        boardGame.baseExpansionStatus = BaseExpansionStatus.BASE
                    }
                }
                boardGame.expansionNames = getExpansionNamesByBoardGameID(boardGame.id!!)
                boardGame.comment = cursor.getStringOrNull(14)
                //TODO:thumbnail
                val tmpThumbnail = cursor.getBlobOrNull(15)
                if (tmpThumbnail != null) {
                    boardGame.thumbnail =
                        BitmapFactory.decodeByteArray(tmpThumbnail, 0, tmpThumbnail.size)
                }
                val locationPair = getLocationNameAndCommentByBoardGameID(boardGame.id!!)
                boardGame.location = locationPair.first
                boardGame.locationComment = locationPair.second
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
        values.put(COLUMN_BASE_EXPANSION_STATUS, boardGame.baseExpansionStatus.name)
        values.put(COLUMN_COMMENT, boardGame.comment)
        val byteArrayOutputStream = ByteArrayOutputStream()
        boardGame.thumbnail?.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        values.put(COLUMN_THUMBNAIL, byteArrayOutputStream.toByteArray())

        db.insert(TABLE_BOARDGAMES, null, values)
        db.close()
    }
}