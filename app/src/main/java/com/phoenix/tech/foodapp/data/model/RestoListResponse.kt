package com.phoenix.tech.foodapp.data.model

import android.content.Context
import com.phoenix.tech.foodapp.utils.parseDebugJson
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


// data.cards[1].card.card.gridElements.infoWithStyle.info.restaurants  => ~20 items
// data.cards[4].card.card.gridElements.infoWithStyle.info.restaurants  => ~8 items

@Serializable
data class RestoListResponse(
    val data: RestoListDataContent,
    val csrfToken: String? = null // used for pagination
)

@Serializable
data class RestoListPageOffset(
    val nextOffset: String? = null,
    val widgetOffset: Map<String, String>? = null
)

@Serializable data class RestoListDataContent(
    val cards: List<RestoListOuterCardWrapper>,
    val pageOffset: RestoListPageOffset? = null // cursor for the next page
)

@Serializable data class RestoListOuterCardWrapper(val card: RestoListInnerCardWrapper)
@Serializable data class RestoListInnerCardWrapper(val card: RestoListGridElementsWrapper)

@Serializable
data class RestoListGridElementsWrapper(
    val gridElements: RestoListGridElements? = null
)

@Serializable
data class RestoListGridElements(
    val infoWithStyle: RestoListInfo? = null
)

@Serializable
data class RestoListInfo(
    val restaurants: List<RestoListingItem> = emptyList()
)

@Serializable
data class RestoListingItem(
    val info: RestoListingItemData,
    val cta: RestoListingItemCta? = null // for click handling
)

// Request body for fetching "/update" i.e. next restaurant items in pagination
@Serializable
data class RestoListUpdateRequest(
    val lat: Double,
    val lng: Double,
    val nextOffset: String?,
    val widgetOffset: Map<String, String>?,
    @SerialName("_csrf") val csrf: String?,
    @SerialName("page_type") val pageType: String = "DESKTOP_WEB_LISTING"
)


@Serializable
data class RestoListingItemData(
    val id: String,
    val name: String,
    val cloudinaryImageId: String,

    val avgRating: Double,
    val totalRatingsString: String,

    val locality: String,
    val areaName: String,
    val cuisines: List<String>,

    val sla: RestoListingItemSla
)

@Serializable
data class RestoListingItemCta(
    val link: String,        // The deep-link or URL
    val type: String
)

@Serializable
data class RestoListingItemSla(
    val deliveryTime: Int,
    val slaString: String,
    val lastMileTravelString: String
)

object SampleRestoListing {
    fun parsedResponse(context: Context): RestoListResponse {
        return context.parseDebugJson("sample_data/resto_list_response.json")
    }

    fun sampleRestoListingItem(context: Context): RestoListingItemData {
        return try {
            parsedResponse(context)
                .data.cards.getOrNull(1)?.card?.card?.gridElements?.infoWithStyle?.restaurants
                ?.firstOrNull()?.info
                ?: fallbackRestoListingItem
        } catch (_: Error) {
            fallbackRestoListingItem
        }
    }

    private val fallbackRestoListingItem = RestoListingItemData(
        id = "732719",
        name = "Theobroma",
        cloudinaryImageId = "RX_THUMBNAIL/IMAGES/VENDOR/2026/3/5/7cdc93ac-fc55-47f8-a908-ed8bf95cf5fd_732719.JPG",
        cuisines = listOf("Desserts"),
        avgRating = 4.4,
        totalRatingsString = "1.7k+",
        locality = "Hinjawadi",
        areaName = "Hinjawadi",
        sla = RestoListingItemSla(
            deliveryTime = 29,
            slaString = "25-30 mins",
            lastMileTravelString = "1.6 km"
        ),
    )
}
