package com.ubiquitous.boardgamecollector

import android.graphics.BitmapFactory
import android.text.Html
import android.util.Log
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.FileNotFoundException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

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
                        //year validation
                        val yearString = yearElement.getAttribute("value")
                        boardGame.yearPublished = if (yearString.isBlank()) {
                            null
                        } else {
                            val year = yearString.toInt()
                            if (year < 0) {
                                null
                            } else {
                                year
                            }
                        }
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
                        //year validation
                        if (boardGame.yearPublished != null && boardGame.yearPublished!! < 0){
                            boardGame.yearPublished = null
                        }

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

    //designers, artists and expansions inside BoardGame object
    fun loadBoardGame(bggid: Int, onlyRank: Boolean = false): BoardGame {
        val boardGame = BoardGame(bggid = bggid)
        Log.i("loadBoardGame", "bggid=$bggid")
        if (bggid > 0) {
            var i = 0
            while (i in 0..5) { //try up to 6 times to load from API
                try {
                    val url = URL("$baseURL/thing?id=$bggid&stats=1")
                    val builderFactory: DocumentBuilderFactory =
                        DocumentBuilderFactory.newInstance()
                    val docBuilder: DocumentBuilder = builderFactory.newDocumentBuilder()
                    val doc: Document = docBuilder.parse(url.openStream())
                    val nodeList: NodeList = doc.getElementsByTagName("item")

                    //get 0-th item - the board game
                    if (nodeList.length > 0) {
                        val element: Element = nodeList.item(0) as Element
                        if (element.getAttribute("id") == bggid.toString()) {
                            if (!onlyRank) { //load everything, if not - only rank

                                //assign status as expansion if is expansion
                                if (element.getAttribute("type") == "boardgameexpansion") {
                                    boardGame.baseExpansionStatus = BaseExpansionStatus.EXPANSION
                                }

                                //assign name
                                val nameNodeList = element.getElementsByTagName("name")
                                val alternateNames = mutableListOf<String>()
                                for (i in 0 until nameNodeList.length) {
                                    if (nameNodeList.item(0).nodeType === Node.ELEMENT_NODE) {
                                        val nameElement: Element = nameNodeList.item(i) as Element
                                        if (nameElement.getAttribute("type") == "primary") {
                                            boardGame.originalName =
                                                nameElement.getAttribute("value")
                                        } else {
                                            alternateNames.add(nameElement.getAttribute("value"))
                                        }
                                    }
                                }
                                boardGame.alternateNames = alternateNames

                                //assign year published
                                val yearNodeList = element.getElementsByTagName("yearpublished")
                                if (yearNodeList.length > 0) {
                                    val yearElement: Element = yearNodeList.item(0) as Element
                                    //year validation
                                    val yearString = yearElement.getAttribute("value")
                                    boardGame.yearPublished = if (yearString.isBlank()) {
                                        null
                                    } else {
                                        val year = yearString.toInt()
                                        if (year < 0) {
                                            null
                                        } else {
                                            year
                                        }
                                    }
                                }

                                //assign description
                                boardGame.description =
                                    Html.fromHtml(getNodeValue("description", element)).toString()

                                //assign thumbnail
                                //val thumbnailURL = getNodeValue("image", element) //load big image
                                val thumbnailURL =
                                    getNodeValue("thumbnail", element) //load small image
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
                                val linkNodeList: NodeList =
                                    element.getElementsByTagName("link") //doc?
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
                                                    expansions[linkElement.getAttribute("id")
                                                        .toInt()] =
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
                            }

                            //assign rank
                            val rankNodeList: NodeList = element.getElementsByTagName("rank")
                            for (i in 0 until rankNodeList.length) {
                                if (rankNodeList.item(0).nodeType === Node.ELEMENT_NODE) {
                                    val rankElement: Element = rankNodeList.item(i) as Element

                                    if (rankElement.getAttribute("type") == "subtype" &&
                                        rankElement.getAttribute("name") == "boardgame"
                                    ) {
                                        try {
                                            if (rankElement.getAttribute("value") != "Not Ranked") {
                                                boardGame.rank =
                                                    rankElement.getAttribute("value").toInt()
                                            }
                                            Log.i(
                                                "loadBoardGame_RANK",
                                                "bggid=$bggid; rank=${rankElement.getAttribute("value")}"
                                            )
                                        } catch (e: Exception) {
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
                    Log.i("loadBoardGame_SUCCESS", "bggid=$bggid")
                    i = -1
                } catch (e: FileNotFoundException) {
                    //wait 3s and try again (up to 6 times)
                    Log.e(
                        "loadBoardGame_FileNotFoundException",
                        "bggid=$bggid; ${e.message}; ${e.stackTraceToString()}"
                    )
                    i++
                    Thread.sleep(1000)
                }
            }
            if (i > 5) {
                Log.i("loadBoardGame_FAILURE", "bggid=$bggid")
            }
        }
        return boardGame
    }
}