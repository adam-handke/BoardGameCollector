package com.ubiquitous.boardgamecollector

import android.graphics.Bitmap
import java.time.LocalDate

class BoardGame(
    var id: Int? = null,
    var name: String? = null,
    var originalName: String? = null,
    var yearPublished: Int? = null,
    var description: String? = null,
    var dateOrdered: LocalDate? = null,
    var dateAdded: LocalDate? = null,
    var pricePurchased: String? = null,
    var rrp: String? = null,    //recommended retail price
    var barcode: String? = null,
    var bggid: Int = 0,
    var mpn: String? = null,    //Manufacturer Part Number
    var rank: Int = 0,
    var baseExtensionStatus: BaseExtensionStatus = BaseExtensionStatus.BASE,
    var comment: String? = null,
    var thumbnail: Bitmap? = null
) {

    //simplest constructor
    constructor(name: String) : this() {
        this.name = name
    }

    override fun toString(): String {
        return name.toString()
    }
}