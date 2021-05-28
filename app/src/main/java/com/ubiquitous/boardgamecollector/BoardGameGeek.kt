package com.ubiquitous.boardgamecollector

import android.graphics.BitmapFactory
import android.util.Log
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory


//TODO: input validation? maybe the API can handle it itself?
//TODO: loading up-to-date rank for a board game (all board games?)
//TODO: parse HTML text to normal text (without weird strings like &#39;)
//TODO: maybe load full-size image instead of small thumbnail?
class BoardGameGeek(
    val baseURL: String = "https://www.boardgamegeek.com/xmlapi2",
    val longLoadingWarningLimit: Int = 20
) {
    //uses DOM parsing based on:
    //https://www.geeksforgeeks.org/xml-parsing-in-android-using-dom-parser/

    private fun getNodeValue(tag: String?, element: Element): String? {
        val nodeList = element.getElementsByTagName(tag)
        val node = nodeList.item(0)
        if (node != null) {
            if (node.hasChildNodes()) {
                val child = node.firstChild
                while (child != null) {
                    if (child.nodeType == Node.TEXT_NODE) {
                        return child.nodeValue
                    }
                }
            }
        }
        // Returns nothing if nothing was found
        return null
    }

    fun searchBoardGamesByName(name: String, asyncTask: BGGActivity.APIAsyncTask): List<BoardGame> {
        val list = mutableListOf<BoardGame>()
        try {
            val url = URL("$baseURL/search?query=$name&type=boardgame")
            val builderFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
            val docBuilder: DocumentBuilder = builderFactory.newDocumentBuilder()
            val doc: Document = docBuilder.parse(url.openStream())
            val nodeList: NodeList = doc.getElementsByTagName("item")

            if (nodeList.length > longLoadingWarningLimit) {
                asyncTask.publicPublishProgress(nodeList.length)
            }

            //iterate through items
            for (i in 0 until nodeList.length) {
                if (nodeList.item(0).nodeType === Node.ELEMENT_NODE && !asyncTask.isCancelled) {
                    val boardGame = BoardGame()
                    val element: Element = nodeList.item(i) as Element

                    //assign bggid
                    boardGame.bggid = element.getAttribute("id").toInt()
                    Log.i("searchBoardGameByName", "bggid=${boardGame.bggid}")

                    //assign name
                    val nameNodeList = element.getElementsByTagName("name")
                    if (nameNodeList.length > 0) {
                        val nameElement: Element = nameNodeList.item(0) as Element
                        if (nameElement.getAttribute("type") == "primary") {
                            boardGame.originalName = nameElement.getAttribute("value")
                        } else {
                            boardGame.name = nameElement.getAttribute("value")
                        }
                    }
                    //assign year published
                    val yearNodeList = element.getElementsByTagName("yearpublished")
                    if (yearNodeList.length > 0) {
                        val yearElement: Element = yearNodeList.item(0) as Element
                        boardGame.yearPublished = yearElement.getAttribute("value").toInt()
                    }

                    list.add(boardGame)
                }
            }
        } catch (e: Exception) {
            Log.e(
                "searchBoardGamesByName_EXCEPTION",
                "name=$name; ${e.message}; ${e.stackTraceToString()}"
            )
        }
        return list
    }

    fun searchBoardGamesByUsername(
        username: String,
        asyncTask: BGGActivity.APIAsyncTask
    ): List<BoardGame> {
        val list = mutableListOf<BoardGame>()
        try {
            val url = URL("$baseURL/collection?username=$username")
            val builderFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
            val docBuilder: DocumentBuilder = builderFactory.newDocumentBuilder()
            val doc: Document = docBuilder.parse(url.openStream())
            val nodeList: NodeList = doc.getElementsByTagName("item")

            if (nodeList.length > longLoadingWarningLimit) {
                asyncTask.publicPublishProgress(nodeList.length)
            }

            //iterate through items
            for (i in 0 until nodeList.length) {
                if (nodeList.item(0).nodeType === Node.ELEMENT_NODE && !asyncTask.isCancelled) {
                    val element: Element = nodeList.item(i) as Element
                    if (element.getAttribute("subtype") == "boardgame") {
                        val boardGame = BoardGame()

                        //assign bggid
                        boardGame.bggid = element.getAttribute("objectid").toInt()
                        Log.i("searchBoardGamesByUsername", "bggid=${boardGame.bggid}")

                        //assign name
                        boardGame.originalName = getNodeValue("name", element)

                        //assign year published
                        boardGame.yearPublished =
                            getNodeValue("yearpublished", element)?.toIntOrNull()

                        //assign comment
                        boardGame.comment = getNodeValue("comment", element)

                        //assign thumbnail
                        /*
                        val thumbnailURL = getNodeValue("thumbnail", element)
                        if (thumbnailURL != null) {
                            val connection: HttpURLConnection =
                                URL(thumbnailURL).openConnection() as HttpURLConnection
                            connection.doInput = true
                            connection.connect()
                            val input: InputStream = connection.inputStream
                            boardGame.thumbnail = BitmapFactory.decodeStream(input)
                        }
                        */

                        list.add(boardGame)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(
                "searchBoardGamesByUsername_EXCEPTION",
                "username=$username; ${e.message}; ${e.stackTraceToString()}"
            )
        }
        return list
    }

    //TODO: rank, designers, artists and expansions inside BoardGame - must be handled afterwards
    fun loadBoardGame(bggid: Int): BoardGame {
        val boardGame = BoardGame()
        if (bggid > 0) {
            try {
                val url = URL("$baseURL/thing?id=$bggid&stats=1")
                val builderFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
                val docBuilder: DocumentBuilder = builderFactory.newDocumentBuilder()
                val doc: Document = docBuilder.parse(url.openStream())
                val nodeList: NodeList = doc.getElementsByTagName("item")

                //get 0-th item - the board game
                if (nodeList.length > 0) {
                    val element: Element = nodeList.item(0) as Element
                    if (element.getAttribute("id") == bggid.toString()) {
                        //assign bggid
                        boardGame.bggid = bggid
                        Log.i("loadBoardGame", "bggid=$bggid")

                        //assign status as expansion if is expansion
                        if (element.getAttribute("type") == "boardgameexpansion") {
                            boardGame.baseExpansionStatus = BaseExpansionStatus.EXPANSION
                        }

                        //assign name
                        val nameNodeList = element.getElementsByTagName("name")
                        if (nameNodeList.length > 0) {
                            val nameElement: Element = nameNodeList.item(0) as Element
                            if (nameElement.getAttribute("type") == "primary") {
                                boardGame.originalName = nameElement.getAttribute("value")
                            }
                            //TODO: boardGame.name
                            // maybe the user should choose out of all names (alternate and primary)?
                            // pop-up window with name choice?
                            // no language indication in the API :(

                        }

                        //assign year published
                        val yearNodeList = element.getElementsByTagName("yearpublished")
                        if (yearNodeList.length > 0) {
                            val yearElement: Element = yearNodeList.item(0) as Element
                            boardGame.yearPublished = yearElement.getAttribute("value").toInt()
                        }

                        //assign description
                        boardGame.description = getNodeValue("description", element)

                        //assign thumbnail
                        val thumbnailURL = getNodeValue("thumbnail", element)
                        if (thumbnailURL != null) {
                            val connection: HttpURLConnection =
                                URL(thumbnailURL).openConnection() as HttpURLConnection
                            connection.doInput = true
                            connection.connect()
                            val input: InputStream = connection.inputStream
                            boardGame.thumbnail = BitmapFactory.decodeStream(input)
                        }

                        //assign artists, designers and expansions (+Base/Expansion status)
                        val artists = mutableMapOf<Int, String?>()
                        val designers = mutableMapOf<Int, String?>()
                        val expansions = mutableMapOf<Int, String?>()
                        val linkNodeList: NodeList = doc.getElementsByTagName("link")
                        for (i in 0 until linkNodeList.length) {
                            if (linkNodeList.item(0).nodeType === Node.ELEMENT_NODE) {
                                val linkElement: Element = linkNodeList.item(i) as Element
                                when (linkElement.getAttribute("type")) {
                                    "boardgameartist" -> {
                                        artists[linkElement.getAttribute("id").toInt()] =
                                            linkElement.getAttribute("value")
                                    }
                                    "boardgamedesigner" -> {
                                        designers[linkElement.getAttribute("id").toInt()] =
                                            linkElement.getAttribute("value")
                                    }
                                    "boardgameexpansion" -> {
                                        if (linkElement.getAttribute("inbound") == "true") {
                                            if (expansions.isEmpty()) {
                                                boardGame.baseExpansionStatus =
                                                    BaseExpansionStatus.EXPANSION
                                            } else {
                                                boardGame.baseExpansionStatus =
                                                    BaseExpansionStatus.BOTH
                                            }
                                        } else {
                                            expansions[linkElement.getAttribute("id").toInt()] =
                                                linkElement.getAttribute("value")
                                            if (boardGame.baseExpansionStatus == BaseExpansionStatus.EXPANSION) {
                                                boardGame.baseExpansionStatus =
                                                    BaseExpansionStatus.BOTH
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        boardGame.artists = artists
                        boardGame.designers = designers
                        boardGame.expansions = expansions

                        //assign rank
                        val statisticsNodeList = element.getElementsByTagName("statistics")
                        if (statisticsNodeList.length > 0) {
                            val statisticsElement: Element = statisticsNodeList.item(0) as Element

                            val ranksNodeList = statisticsElement.getElementsByTagName("ranks")
                            if (ranksNodeList.length > 0) {
                                val ranksElement: Element = ranksNodeList.item(0) as Element

                                val rankNodeList: NodeList =
                                    ranksElement.getElementsByTagName("rank")
                                for (i in 0 until rankNodeList.length) {
                                    if (rankNodeList.item(0).nodeType === Node.ELEMENT_NODE) {
                                        val rankElement: Element = rankNodeList.item(i) as Element

                                        if (rankElement.getAttribute("type") == "subtype" &&
                                            rankElement.getAttribute("name") == "boardgame"
                                        ) {
                                            try {
                                                boardGame.rank =
                                                    rankElement.getAttribute("value").toInt()
                                            } catch (e: Exception){
                                                Log.e(
                                                    "loadBoardGame_RANK_EXCEPTION",
                                                    "bggid=$bggid; ${e.message}; ${e.stackTraceToString()}"
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(
                    "loadBoardGame_EXCEPTION",
                    "bggid=$bggid; ${e.message}; ${e.stackTraceToString()}"
                )
            }
        }
        return boardGame
    }
}