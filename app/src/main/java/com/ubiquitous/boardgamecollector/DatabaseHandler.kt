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
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.Exception

class DatabaseHandler(
    context: Context,
    name: String? = DATABASE_NAME,
    factory: SQLiteDatabase.CursorFactory?,
    ver: Int = DATABASE_VERSION
) : SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    //private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "BoardGameCollectorDB.db"
        private var instance: DatabaseHandler? = null

        //singleton
        fun getInstance(context: Context): DatabaseHandler {
            if (instance == null) {
                instance = DatabaseHandler(context, DATABASE_NAME, null, DATABASE_VERSION)
            }
            return instance!!
        }

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
        const val COLUMN_ID = "_id"
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
                    "    $COLUMN_BOARDGAME_ID INTEGER REFERENCES $TABLE_BOARDGAMES ($COLUMN_ID)" +
                    "                         ON DELETE CASCADE,\n" +
                    "    $COLUMN_ARTIST_ID    INTEGER REFERENCES $TABLE_ARTISTS ($COLUMN_ID) " +
                    "                         ON DELETE CASCADE,\n" +
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
                    "    $COLUMN_BOARDGAME_ID   INTEGER REFERENCES $TABLE_BOARDGAMES ($COLUMN_ID) " +
                    "                           ON DELETE CASCADE,\n" +
                    "    $COLUMN_DESIGNER_ID    INTEGER REFERENCES $TABLE_DESIGNERS ($COLUMN_ID)" +
                    "                           ON DELETE CASCADE,\n" +
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
                    "                           ON DELETE CASCADE UNIQUE,\n" +
                    "    $COLUMN_LOCATION_ID    INTEGER REFERENCES $TABLE_LOCATIONS ($COLUMN_ID)" +
                    "                           ON DELETE CASCADE,\n" +
                    "    $COLUMN_COMMENT        TEXT,\n" +
                    "                           PRIMARY KEY (\n" +
                    "                               $COLUMN_BOARDGAME_ID,\n" +
                    "                               $COLUMN_LOCATION_ID\n" +
                    "    )\n" +
                    ");"
        )
        db?.execSQL(
            "CREATE TABLE $LINK_TABLE_BOARDGAMES_EXPANSIONS (\n" +
                    "    $COLUMN_BOARDGAME_ID    INTEGER REFERENCES $TABLE_BOARDGAMES ($COLUMN_ID) " +
                    "                            ON DELETE CASCADE,\n" +
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
                    "    $COLUMN_BOARDGAME_ID   INTEGER REFERENCES $TABLE_BOARDGAMES ($COLUMN_ID) " +
                    "                           ON DELETE CASCADE\n" +
                    "                           NOT NULL,\n" +
                    "    $COLUMN_RANK           INTEGER NOT NULL\n" +
                    "                           DEFAULT (0),\n" +
                    "    $COLUMN_DATE_RETRIEVED DATE    NOT NULL\n" +
                    ");"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        try {
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
            Log.i("onUpgrade", "DONE")
        } catch (e: Exception) {
            Log.e("onUpgrade_EXCEPTION", "${e.message}; ${e.stackTraceToString()}")
        }
    }

    fun getAllBoardGamesWithoutDetails(): List<BoardGame> {
        val list: MutableList<BoardGame> = mutableListOf()
        /*
        val query =
            "SELECT $COLUMN_ID, $COLUMN_NAME, $COLUMN_YEAR_PUBLISHED, $COLUMN_DESCRIPTION, $COLUMN_DATE_ADDED, " +
                    "$COLUMN_BGGID, $COLUMN_RANK, $COLUMN_BASE_EXPANSION_STATUS, $COLUMN_THUMBNAIL " +
                    "FROM $TABLE_BOARDGAMES;"
         */
        val db = this.writableDatabase
        val cursor = db.query(
            TABLE_BOARDGAMES,
            arrayOf(
                COLUMN_ID,
                COLUMN_NAME,
                COLUMN_ORIGINAL_NAME,
                COLUMN_YEAR_PUBLISHED,
                //COLUMN_DESCRIPTION,
                //COLUMN_DATE_ADDED,
                COLUMN_BGGID,
                COLUMN_RANK,
                COLUMN_BASE_EXPANSION_STATUS,
                COLUMN_THUMBNAIL
            ),
            null,
            null,
            null,
            null,
            null,
            null
        )
        try {
            while (cursor.moveToNext()) {
                val boardGame = BoardGame()
                boardGame.id = cursor.getIntOrNull(0)
                boardGame.name = cursor.getStringOrNull(1)
                boardGame.originalName = cursor.getStringOrNull(2)
                boardGame.yearPublished = cursor.getIntOrNull(3)
                /*
                boardGame.description = cursor.getStringOrNull(4)
                boardGame.dateAdded =
                    cursor.getLongOrNull(5)?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                 */
                boardGame.bggid = cursor.getInt(4)
                boardGame.rank = cursor.getInt(5)
                when (cursor.getString(6)) {
                    BaseExpansionStatus.EXPANSION.name -> boardGame.baseExpansionStatus =
                        BaseExpansionStatus.EXPANSION
                    BaseExpansionStatus.BOTH.name -> boardGame.baseExpansionStatus =
                        BaseExpansionStatus.BOTH
                    else -> {
                        boardGame.baseExpansionStatus = BaseExpansionStatus.BASE
                    }
                }
                val tmpThumbnail = cursor.getBlobOrNull(7)
                if (tmpThumbnail != null) {
                    boardGame.thumbnail =
                        BitmapFactory.decodeByteArray(tmpThumbnail, 0, tmpThumbnail.size)
                }
                list.add(boardGame)
            }
        } catch (e: Exception) {
            Log.e("getAllBoardGames_EXCEPTION", "${e.message}; ${e.stackTraceToString()}")
        } finally {
            cursor.close()
        }
        db.close()
        return list
    }

    fun countBoardGames(): Int {
        var count = 0

        val db = this.writableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_BOARDGAMES", null)
        try {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0)
            }
        } catch (e: Exception) {
            Log.e("countBoardGames_EXCEPTION", "${e.message}; ${e.stackTraceToString()}")
        } finally {
            cursor.close()
        }
        db.close()

        return count
    }

    fun getAllLocations(): Map<Int, String?> {
        val map: MutableMap<Int, String?> = mutableMapOf()

        val db = this.writableDatabase
        val cursor = db.query(
            TABLE_LOCATIONS,
            arrayOf(
                COLUMN_ID,
                COLUMN_NAME
            ),
            null,
            null,
            null,
            null,
            null,
            null
        )
        try {
            while (cursor.moveToNext()) {
                map[cursor.getInt(0)] = cursor.getStringOrNull(1)
            }
        } catch (e: Exception) {
            Log.e("getAllLocations_EXCEPTION", "${e.message}; ${e.stackTraceToString()}")
        } finally {
            cursor.close()
        }
        db.close()

        return map
    }

    private fun getArtists(boardGameID: Int): Map<Int, String?> {
        val map = mutableMapOf<Int, String?>()
        if (boardGameID > 0) {
            val query = "SELECT $TABLE_ARTISTS.$COLUMN_ID, $TABLE_ARTISTS.$COLUMN_NAME " +
                    "FROM $LINK_TABLE_BOARDGAMES_ARTISTS INNER JOIN $TABLE_ARTISTS " +
                    "ON $LINK_TABLE_BOARDGAMES_ARTISTS.$COLUMN_ARTIST_ID = $TABLE_ARTISTS.$COLUMN_ID " +
                    "WHERE $LINK_TABLE_BOARDGAMES_ARTISTS.$COLUMN_BOARDGAME_ID = $boardGameID " +
                    "ORDER BY $TABLE_ARTISTS.$COLUMN_NAME;"
            val db = this.writableDatabase
            val cursor = db.rawQuery(query, null)
            try {
                while (cursor.moveToNext()) {
                    map[cursor.getInt(0)] = cursor.getStringOrNull(1)
                }
            } catch (e: Exception) {
                Log.e("getArtists_EXCEPTION", "${e.message}; ${e.stackTraceToString()}")
            } finally {
                cursor.close()
            }
            db.close()
        }
        return map
    }

    private fun getDesigners(boardGameID: Int): Map<Int, String?> {
        val map = mutableMapOf<Int, String?>()
        if (boardGameID > 0) {
            val query = "SELECT $TABLE_DESIGNERS.$COLUMN_ID, $TABLE_DESIGNERS.$COLUMN_NAME " +
                    "FROM $LINK_TABLE_BOARDGAMES_DESIGNERS INNER JOIN $TABLE_DESIGNERS " +
                    "ON $LINK_TABLE_BOARDGAMES_DESIGNERS.$COLUMN_DESIGNER_ID = $TABLE_DESIGNERS.$COLUMN_ID " +
                    "WHERE $LINK_TABLE_BOARDGAMES_DESIGNERS.$COLUMN_BOARDGAME_ID = $boardGameID " +
                    "ORDER BY $TABLE_DESIGNERS.$COLUMN_NAME;"
            val db = this.writableDatabase
            val cursor = db.rawQuery(query, null)
            try {
                while (cursor.moveToNext()) {
                    map[cursor.getInt(0)] = cursor.getStringOrNull(1)
                }
            } catch (e: Exception) {
                Log.e("getDesigners_EXCEPTION", "${e.message}; ${e.stackTraceToString()}")
            } finally {
                cursor.close()
            }
            db.close()
        }
        return map
    }

    private fun getLocationNameAndComment(boardGameID: Int): Pair<String?, String?> {
        //first - location name
        //second - comment on location
        var pair = Pair<String?, String?>(null, null)
        if (boardGameID > 0) {
            val query =
                "SELECT $TABLE_LOCATIONS.$COLUMN_NAME, $LINK_TABLE_BOARDGAMES_LOCATIONS.$COLUMN_COMMENT " +
                        "FROM $LINK_TABLE_BOARDGAMES_LOCATIONS INNER JOIN $TABLE_LOCATIONS " +
                        "ON $LINK_TABLE_BOARDGAMES_LOCATIONS.$COLUMN_LOCATION_ID = $TABLE_LOCATIONS.$COLUMN_ID " +
                        "WHERE $LINK_TABLE_BOARDGAMES_LOCATIONS.$COLUMN_BOARDGAME_ID = $boardGameID " +
                        "ORDER BY $TABLE_LOCATIONS.$COLUMN_NAME;"
            val db = this.writableDatabase
            val cursor = db.rawQuery(query, null)
            try {
                if (cursor.moveToFirst()) {
                    pair = Pair(cursor.getStringOrNull(0), cursor.getStringOrNull(1))
                }
            } catch (e: Exception) {
                Log.e(
                    "getLocationNameAndComment_EXCEPTION",
                    "${e.message}; ${e.stackTraceToString()}"
                )
            } finally {
                cursor.close()
            }
            db.close()
        }
        return pair
    }

    fun getLocationID(boardGameID: Int): Int {
        var locationID = 0
        if (boardGameID > 0) {
            val db = this.writableDatabase
            val cursor = db.query(
                LINK_TABLE_BOARDGAMES_LOCATIONS,
                arrayOf(
                    COLUMN_LOCATION_ID
                ),
                "$COLUMN_BOARDGAME_ID = ?",
                arrayOf(boardGameID.toString()),
                null,
                null,
                null,
                null
            )
            try {
                if (cursor.moveToFirst()) {
                    locationID = cursor.getInt(0)
                }
            } catch (e: Exception) {
                Log.e("getLocationID_EXCEPTION", "${e.message}; ${e.stackTraceToString()}")
            } finally {
                cursor.close()
            }
            db.close()
        }
        return locationID
    }

    fun getLocationComment(boardGameID: Int, locationID: Int): String? {
        var locationComment: String? = null
        if (boardGameID > 0 && locationID > 0) {
            val db = this.writableDatabase
            val cursor = db.query(
                LINK_TABLE_BOARDGAMES_LOCATIONS,
                arrayOf(
                    COLUMN_COMMENT
                ),
                "$COLUMN_BOARDGAME_ID = ? AND $COLUMN_LOCATION_ID = ?",
                arrayOf(boardGameID.toString(), locationID.toString()),
                null,
                null,
                null,
                null
            )
            try {
                if (cursor.moveToFirst()) {
                    locationComment = cursor.getStringOrNull(0)
                }
            } catch (e: Exception) {
                Log.e("getLocationComment_EXCEPTION", "${e.message}; ${e.stackTraceToString()}")
            } finally {
                cursor.close()
            }
            db.close()
        }
        return locationComment
    }

    private fun getExpansions(boardGameID: Int): Map<Int, String?> {
        val map = mutableMapOf<Int, String?>()
        if (boardGameID > 0) {
            /*
            val query = "SELECT $COLUMN_EXPANSION_NAME FROM $ " +
                    "WHERE $COLUMN_BOARDGAME_ID = $id ORDER BY $COLUMN_EXPANSION_NAME;"
             */
            val db = this.writableDatabase
            val cursor = db.query(
                LINK_TABLE_BOARDGAMES_EXPANSIONS,
                arrayOf(COLUMN_EXPANSION_BGGID, COLUMN_EXPANSION_NAME),
                "$COLUMN_BOARDGAME_ID = ?",
                arrayOf(boardGameID.toString()),
                null,
                null,
                COLUMN_EXPANSION_NAME,
                null
            )
            try {
                while (cursor.moveToNext()) {
                    map[cursor.getInt(0)] = cursor.getStringOrNull(1)
                }
            } catch (e: Exception) {
                Log.e("getExpansionNames_EXCEPTION", "${e.message}; ${e.stackTraceToString()}")
            } finally {
                cursor.close()
            }
            db.close()
        }
        return map
    }

    fun getRankHistory(boardGameID: Int): List<Pair<Int, LocalDate>> {
        val list = mutableListOf<Pair<Int, LocalDate>>()
        if (boardGameID > 0) {
            val db = this.writableDatabase
            val query = "SELECT $COLUMN_RANK, $COLUMN_DATE_RETRIEVED FROM $TABLE_RANK_HISTORY " +
                    "WHERE $COLUMN_BOARDGAME_ID = $boardGameID ORDER BY $COLUMN_DATE_RETRIEVED DESC;"
            val cursor = db.rawQuery(query, null)
            try {
                while (cursor.moveToNext()) {
                    val tmpRank = cursor.getInt(0)
                    val tmpDate =
                        Instant.ofEpochMilli(cursor.getLong(1)).atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    list.add(Pair(tmpRank, tmpDate))
                }
            } catch (e: Exception) {
                Log.e("getRankHistory_EXCEPTION", "${e.message}; ${e.stackTraceToString()}")
            } finally {
                cursor.close()
            }
            db.close()
        }
        return list
    }

    fun getBoardGameDetails(boardGameID: Int): BoardGame {
        val boardGame = BoardGame()
        //val query = "SELECT * FROM $TABLE_BOARDGAMES WHERE $COLUMN_ID = $id;"
        val db = this.writableDatabase
        val cursor = db.query(
            TABLE_BOARDGAMES,
            null,
            "$COLUMN_ID = ?",
            arrayOf(boardGameID.toString()),
            null,
            null,
            null,
            null
        )
        try {
            if (cursor.moveToFirst()) {
                boardGame.id = cursor.getInt(0)
                boardGame.name = cursor.getStringOrNull(1)
                boardGame.originalName = cursor.getStringOrNull(2)
                boardGame.yearPublished = cursor.getIntOrNull(3)
                boardGame.designers = getDesigners(boardGameID)
                boardGame.artists = getArtists(boardGameID)
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
                boardGame.expansions = getExpansions(boardGameID)
                boardGame.comment = cursor.getStringOrNull(14)
                val tmpThumbnail = cursor.getBlobOrNull(15)
                if (tmpThumbnail != null) {
                    boardGame.thumbnail =
                        BitmapFactory.decodeByteArray(tmpThumbnail, 0, tmpThumbnail.size)
                }
                val locationPair = getLocationNameAndComment(boardGameID)
                boardGame.locationName = locationPair.first
                boardGame.locationComment = locationPair.second
            }
        } catch (e: Exception) {
            Log.e("getBoardGameDetails_EXCEPTION", "${e.message}; ${e.stackTraceToString()}")
        } finally {
            cursor.close()
        }
        db.close()
        return boardGame
    }

    //TODO: artists, designers, expansions
    fun insertBoardGame(boardGame: BoardGame, locationID: Int): Int {
        val db = this.writableDatabase

        val values = ContentValues()
        //values.put(COLUMN_ID, boardGame.id)
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
        //values.put(COLUMN_RANK, boardGame.rank) //done separately below, with rank history
        values.put(COLUMN_BASE_EXPANSION_STATUS, boardGame.baseExpansionStatus.name)
        values.put(COLUMN_COMMENT, boardGame.comment)
        val byteArrayOutputStream = ByteArrayOutputStream()
        boardGame.thumbnail?.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        values.put(COLUMN_THUMBNAIL, byteArrayOutputStream.toByteArray())

        val id = db.insert(TABLE_BOARDGAMES, null, values).toInt()
        Log.i("insertBoardGame", "id=$id")
        db.close()
        if (locationID > 0) {
            //new board game in existing location
            updateLocationOfBoardGame(id, locationID, boardGame.locationComment)
        }
        //Log.i("insertBoardGame", "rank=${boardGame.rank}")
        if (boardGame.rank > 0) {
            //save rank in rank history
            insertRankHistoryRecord(id, boardGame.rank)
        }
        if (boardGame.expansions.isNotEmpty()) {
            //insert expansions
            for (expansion in boardGame.expansions) {
                Log.i(
                    "insertBoardGame_EXPANSION",
                    "id=$id; expansionBGGID=${expansion.key}; expansion_name=${expansion.value}"
                )
                insertExpansionOfBoardGame(id, expansion.key, expansion.value ?: "")
            }
        }

        if (boardGame.artists.isNotEmpty()) {
            //insert artists
            for (artist in boardGame.artists) {
                Log.i(
                    "insertBoardGame_ARTIST",
                    "id=$id; artistBGGID=${artist.key}; artist_name=${artist.value}"
                )
                insertArtistOfBoardGame(id, artist.key, artist.value ?: "")
            }
        }
        if (boardGame.designers.isNotEmpty()) {
            //insert designers
            for (designer in boardGame.designers) {
                Log.i(
                    "insertBoardGame_DESIGNER",
                    "id=$id; designerBGGID=${designer.key}; designer_name=${designer.value}"
                )
                insertDesignerOfBoardGame(id, designer.key, designer.value ?: "")
            }
        }
        return id
    }

    //only automatically from BGG
    private fun insertArtistOfBoardGame(
        boardGameID: Int, artistBGGID: Int = 0, artistName: String
    ): Int {
        val db = this.writableDatabase
        //check if artist with the same BGGID exists in the DB
        var artistID = 0
        if (artistBGGID > 0 && artistName.isNotBlank()) {
            //val query = "SELECT $COLUMN_ID FROM $TABLE_ARTISTS WHERE $COLUMN_BGGID = $artistBGGID;"
            val cursor = db.query(
                TABLE_ARTISTS,
                arrayOf(COLUMN_ID),
                "$COLUMN_BGGID = ?",
                arrayOf(artistBGGID.toString()),
                null,
                null,
                null,
                null
            )
            try {
                if (cursor.moveToFirst()) {
                    artistID = cursor.getInt(0)
                }
            } catch (e: Exception) {
                Log.e(
                    "insertArtistOfBoardGame_EXCEPTION",
                    "${e.message}; ${e.stackTraceToString()}"
                )
            } finally {
                cursor.close()
            }
            if (artistID == 0) {
                val values = ContentValues()
                values.put(COLUMN_BGGID, artistBGGID)
                values.put(COLUMN_NAME, artistName)
                artistID = db.insert(TABLE_ARTISTS, null, values).toInt()
                Log.i("insertArtistOfBoardGame", "id=$artistID")
            }
            val values = ContentValues()
            values.put(COLUMN_BOARDGAME_ID, boardGameID)
            values.put(COLUMN_ARTIST_ID, artistID)
            db.insert(LINK_TABLE_BOARDGAMES_ARTISTS, null, values)
        }
        db.close()
        return artistID
    }

    //only automatically from BGG
    private fun insertDesignerOfBoardGame(
        boardGameID: Int, designerBGGID: Int = 0, designerName: String
    ): Int {
        val db = this.writableDatabase
        var designerID = 0
        if (designerBGGID > 0 && designerName.isNotBlank()) {
            //check if designer with the same BGGID exists in the DB
            //val query = "SELECT $COLUMN_ID FROM $TABLE_DESIGNERS WHERE $COLUMN_BGGID = $designerBGGID;"
            val cursor = db.query(
                TABLE_DESIGNERS,
                arrayOf(COLUMN_ID),
                "$COLUMN_BGGID = ?",
                arrayOf(designerBGGID.toString()),
                null,
                null,
                null,
                null
            )
            try {
                if (cursor.moveToFirst()) {
                    designerID = cursor.getInt(0)
                }
            } catch (e: Exception) {
                Log.e(
                    "insertDesignerOfBoardGame_EXCEPTION",
                    "${e.message}; ${e.stackTraceToString()}"
                )
            } finally {
                cursor.close()
            }
            if (designerID == 0) {
                val values = ContentValues()
                values.put(COLUMN_BGGID, designerBGGID)
                values.put(COLUMN_NAME, designerName)
                designerID = db.insert(TABLE_DESIGNERS, null, values).toInt()
                Log.i("insertDesignerOfBoardGame", "id=$designerID")
            }
            val values = ContentValues()
            values.put(COLUMN_BOARDGAME_ID, boardGameID)
            values.put(COLUMN_DESIGNER_ID, designerID)
            db.insert(LINK_TABLE_BOARDGAMES_DESIGNERS, null, values)
        }
        db.close()
        return designerID
    }

    fun insertLocationOfBoardGame(
        boardGameID: Int,
        locationName: String,
        locationComment: String? = null
    ): Int {
        val db = this.writableDatabase
        //check if location with the same name exists in the DB
        var locationID = 0
        if (locationName.isNotEmpty()) {
            //val query = "SELECT $COLUMN_ID FROM $TABLE_LOCATIONS WHERE $COLUMN_NAME = ?;"
            val cursor = db.query(
                TABLE_LOCATIONS,
                arrayOf(COLUMN_ID),
                "$COLUMN_NAME = ?",
                arrayOf(locationName),
                null,
                null,
                null,
                null
            )
            try {
                if (cursor.moveToFirst()) {
                    locationID = cursor.getInt(0)
                }
            } catch (e: Exception) {
                Log.e(
                    "insertLocationOfBoardGame_EXCEPTION",
                    "${e.message}; ${e.stackTraceToString()}"
                )
            } finally {
                cursor.close()
            }
        }
        if (locationID == 0) {
            val values = ContentValues()
            values.put(COLUMN_NAME, locationName)
            locationID = db.insert(TABLE_LOCATIONS, null, values).toInt()
            Log.i("insertLocationOfBoardGame", "id=$locationID")
        }
        val values = ContentValues()
        values.put(COLUMN_BOARDGAME_ID, boardGameID)
        values.put(COLUMN_LOCATION_ID, locationID)
        if (locationComment == null) {
            values.putNull(COLUMN_COMMENT)
        } else {
            values.put(COLUMN_COMMENT, locationComment)
        }
        db.insert(LINK_TABLE_BOARDGAMES_LOCATIONS, null, values)
        db.close()
        return locationID
    }

    //applicable only for expansions loaded from BGGID
    //TODO: modify to allow user-provided expansions
    private fun insertExpansionOfBoardGame(
        boardGameID: Int,
        expansionBGGID: Int,
        expansionName: String
    ) {
        if (expansionBGGID > 0 && expansionName.isNotBlank()) {
            val db = this.writableDatabase
            /*
            val query =
                "SELECT * FROM $LINK_TABLE_BOARDGAMES_EXPANSIONS " +
                        "WHERE $COLUMN_BOARDGAME_ID = $boardGameID " +
                        "AND $COLUMN_EXPANSION_BGGID = ${expansion.bggid};"
             */
            val cursor = db.query(
                LINK_TABLE_BOARDGAMES_EXPANSIONS,
                null,
                "$COLUMN_BOARDGAME_ID = ? AND $COLUMN_EXPANSION_BGGID = ?",
                arrayOf(
                    boardGameID.toString(),
                    expansionBGGID.toString()
                ),
                null,
                null,
                null,
                null
            )
            if (cursor.count == 0) {
                //only insert when there isn't already such expansion (bggid) connected to that ID
                cursor.close()
                val values = ContentValues()
                values.put(COLUMN_BOARDGAME_ID, boardGameID)
                values.put(COLUMN_EXPANSION_BGGID, expansionBGGID)
                values.put(COLUMN_EXPANSION_NAME, expansionName)
                db.insert(LINK_TABLE_BOARDGAMES_EXPANSIONS, null, values)
            }
            db.close()
        }
    }

    //TODO: check if works correctly when API ranks are back up
    fun insertRankHistoryRecord(boardGameID: Int, rank: Int): Int {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_BOARDGAME_ID, boardGameID)
        values.put(COLUMN_RANK, rank)
        values.put(COLUMN_DATE_RETRIEVED, Instant.now().toEpochMilli())
        val id = db.insert(TABLE_RANK_HISTORY, null, values).toInt()

        //update rank in boardgame record
        val valuesUpdate = ContentValues()
        valuesUpdate.put(COLUMN_RANK, rank)
        db.update(
            TABLE_BOARDGAMES,
            valuesUpdate,
            "$COLUMN_ID = ?",
            arrayOf(boardGameID.toString())
        )
        db.close()

        Log.i("insertRankHistoryRecord", "boardGameID=$boardGameID; rank=$rank; rankRecordID=$id")
        return id
    }

    fun updateBoardGame(boardGame: BoardGame, locationID: Int) {

        if (boardGame.id != null && boardGame.id!! > 0) {
            val db = this.writableDatabase
            //update boardgames table
            val values = ContentValues()
            values.put(COLUMN_NAME, boardGame.name)
            values.put(COLUMN_ORIGINAL_NAME, boardGame.originalName)
            values.put(COLUMN_YEAR_PUBLISHED, boardGame.yearPublished)
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
            //values.put(COLUMN_RANK, boardGame.rank) //rank may only be updated by inserting a new rank history record
            values.put(COLUMN_BASE_EXPANSION_STATUS, boardGame.baseExpansionStatus.name)
            values.put(COLUMN_COMMENT, boardGame.comment)
            val byteArrayOutputStream = ByteArrayOutputStream()
            boardGame.thumbnail?.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            values.put(COLUMN_THUMBNAIL, byteArrayOutputStream.toByteArray())
            val rows = db.update(
                TABLE_BOARDGAMES,
                values,
                "$COLUMN_ID = ?",
                arrayOf(boardGame.id.toString())
            )
            Log.i("updateBoardGame", "updated $rows BoardGames rows")

            db.close()

            //update location
            updateLocationOfBoardGame(boardGame.id!!, locationID, boardGame.locationComment)
        }
    }

    fun updateLocationOfBoardGame(boardGameID: Int, locationID: Int, locationComment: String?) {
        if (boardGameID > 0 && locationID > 0) {
            val db = this.writableDatabase

            db.delete(
                LINK_TABLE_BOARDGAMES_LOCATIONS,
                "$COLUMN_BOARDGAME_ID = ?",
                arrayOf(boardGameID.toString())
            )

            val valuesLocations = ContentValues()
            valuesLocations.put(COLUMN_BOARDGAME_ID, boardGameID)
            valuesLocations.put(COLUMN_LOCATION_ID, locationID)
            valuesLocations.put(COLUMN_COMMENT, locationComment)
            db.insert(LINK_TABLE_BOARDGAMES_LOCATIONS, null, valuesLocations)

            db.close()
        }
    }

    fun deleteBoardGame(boardGameID: Int) {

        if (boardGameID > 0) {
            val db = this.writableDatabase
            val rows =
                db.delete(TABLE_BOARDGAMES, "$COLUMN_ID = ?", arrayOf(boardGameID.toString()))
            Log.i("deleteBoardGame", "deleted $rows BoardGames rows where id=$boardGameID")
            db.close()
        }
    }
}