package com.ubiquitous.boardgamecollector

import android.graphics.Bitmap
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

//TODO: add artist, designer, location
class BoardGame(
    var id: Int? = null,
    var name: String? = null,
    var originalName: String? = null,
    var yearPublished: Int? = null,
    var designers: Map<Int, String?> = mapOf(),  //id/bggid - name
    var artists: Map<Int, String?> = mapOf(),    //id/bggid - name
    var description: String? = null,
    var dateOrdered: LocalDate? = null,
    var dateAdded: LocalDate? = null,
    var pricePurchased: String? = null,
    var rrp: String? = null,    //recommended retail price
    var barcode: String? = null,
    var bggid: Int = 0,         //ID in the BoardGameGeek database
    var mpn: String? = null,    //Manufacturer Part Number
    var rank: Int = 0,
    var baseExpansionStatus: BaseExpansionStatus = BaseExpansionStatus.BASE,
    var expansions: Map<Int, String?> = mapOf(),  //id/bggid - name
    var comment: String? = null,
    var thumbnail: Bitmap? = null,
    var locationName: String? = null,
    var locationComment: String? = null
) {
    private var pattern = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    //simplest constructor
    constructor(name: String) : this() {
        this.name = name
    }

    override fun toString(): String {
        return name.toString()
    }

    fun nameToString(unnamed: String): String {
        return ((name ?: originalName ?: unnamed) + " (" + (yearPublished ?: "—") + ")")
    }

    fun toStringArray(base: String, expansion: String, both: String): Array<String> {
        return arrayOf(
            name ?: "—",
            originalName ?: "—",
            yearPublished?.toString() ?: "—",
            when (designers.size) {
                0 -> "—"
                else -> designers.values.joinToString(", ")
            },
            when (artists.size) {
                0 -> "—"
                else -> artists.values.joinToString(", ")
            },
            description ?: "—",
            dateOrdered?.format(pattern) ?: "—",
            dateAdded?.format(pattern) ?: "—",
            pricePurchased ?: "—",
            rrp ?: "—",
            barcode ?: "—",
            bggid.toString(),
            mpn ?: "—",
            rank.toString(),
            when (baseExpansionStatus) {
                BaseExpansionStatus.EXPANSION -> expansion
                BaseExpansionStatus.BOTH -> both
                else -> base
            },
            when (expansions.size) {
                0 -> "—"
                else -> expansions.values.joinToString(", ")
            },
            comment ?: "—",
            locationName ?: "—",
            locationComment ?: "—"
        )
    }

    fun getThumbnailResizedByWidth(width: Int, limit: Int = 5): Bitmap? {
        //default: limit enlargement to 5x bigger
        return if (thumbnail != null) {
            val ratio: Float = thumbnail!!.width.toFloat() / thumbnail!!.height.toFloat()
            val height: Int = (width / ratio).roundToInt()
            if (thumbnail!!.width * limit < width || thumbnail!!.height * limit < height) {
                Bitmap.createScaledBitmap(
                    thumbnail!!,
                    thumbnail!!.width * limit,
                    thumbnail!!.height * limit,
                    false
                )
            } else {
                Bitmap.createScaledBitmap(thumbnail!!, width, height, false)
            }
        } else {
            null
        }
    }

}