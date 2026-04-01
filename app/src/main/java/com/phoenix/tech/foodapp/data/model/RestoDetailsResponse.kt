package com.phoenix.tech.foodapp.data.model


import android.content.Context
import com.phoenix.tech.foodapp.utils.parseDebugJson
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Food Categories and Items:
// data.cards[5].groupedCard.cardGroupMap.REGULAR.cards[SOME_INDEX] ~ 18 items (only take index 2-15)
//   .card.card
//     .itemCards[SOME_INDEX]  => Individual food-item list
//     .categories[SOME_INDEX] => Sub-categories, each subcategory having own food-item list


// Restaurant info:
// .data.cards[2].card.card.info


@Serializable
data class RestoDetailsResponse(val data: RestoDetailsResponseData)

@Serializable
data class RestoDetailsResponseData(val cards: List<RestoDetailsCardWrapper>)

@Serializable
data class RestoDetailsCardWrapper(
    val groupedCard: RestoDetailsGroupedCard? = null,
    val card: RestoDetailsRestoInfoCardWrapper? = null,
)

@Serializable
data class RestoDetailsGroupedCard(
    val cardGroupMap: RestoDetailsCardGroupMap
)

@Serializable
data class RestoDetailsCardGroupMap(
    @SerialName("REGULAR")
    val regularMenu: RestoDetailsRegularMenu
)

@Serializable
data class RestoDetailsRegularMenu(
    val cards: List<RestoDetailsMenuCardWrapper>
)

@Serializable
data class RestoDetailsMenuCardWrapper(val card: RestoDetailsMenuCardInnerWrapper)

@Serializable
data class RestoDetailsMenuCardInnerWrapper(val card: RestoDetailsMenuCategory)

@Serializable
data class RestoDetailsMenuCategory(
    val title: String? = null,
    val itemCards: List<RestoDetailsCategoryItemWrapper>? = null,

    @SerialName("categories")
    val subCategories: List<RestoDetailsMenuCategory>? = null
)

@Serializable
data class RestoDetailsCategoryItemWrapper(val card: RestoDetailsCategoryItemInnerWrapper)

@Serializable
data class RestoDetailsCategoryItemInnerWrapper(
    val info: RestoDetailsCategoryFoodItem
)

@Serializable
data class RestoDetailsCategoryFoodItem(
    val id: String,
    val name: String,
    val description: String? = null,
    val imageId: String? = null, // Dynamically mapped to various URLs (we can't show)
    val price: Int? = null, // In paise
    val defaultPrice: Int? = null, // When variations/addons
    val isVeg: Int? = null
)

// image URL examples:
// https://media-assets.swiggy.com/swiggy/image/upload/fl_lossy,f_auto,q_auto,w_300,h_300,c_fit/FOOD_CATALOG/IMAGES/CMS/2025/7/22/3f28c9a8-3ac1-42c6-92e8-cee76ada1517_9f16e45e-72ba-4e22-82c1-3fed44e2a57d.jpg
// https://media-assets.swiggy.com/swiggy/image/upload/fl_lossy,f_auto,q_auto,w_300,h_300,c_fit/FOOD_CATALOG/IMAGES/CMS/2025/7/24/80f66335-3532-4d56-8cb1-b3f8ec7389b8_e2b600fd-91a3-4daa-91a5-b4d25b8f2bd1.jpg
// https://media-assets.swiggy.com/swiggy/image/upload/fl_lossy,f_auto,q_auto,w_300,h_300,c_fit/FOOD_CATALOG/IMAGES/CMS/2025/7/21/438d9582-ca7b-4d5b-a200-019e241323b3_a6ae58f5-b44f-4acf-ba95-f9876d7756f8.jpg


@Serializable
data class RestoDetailsRestoInfoCardWrapper(val card: RestoDetailsRestoInfoCard? = null)

@Serializable
data class RestoDetailsRestoInfoCard(
    val info: RestoDetailsRestoInfo? = null
)

@Serializable
data class RestoDetailsRestoInfo(
    val id: String,
    val name: String,
    val cloudinaryImageId: String,
    val cuisines: List<String>,
    val avgRating: Double,
    val totalRatingsString: String,
    val city: String,
    val locality: String,
    val areaName: String,
    val sla: RestoDetailsRestoInfoSla
)


@Serializable
data class RestoDetailsRestoInfoSla(
    val deliveryTime: Int,
    val slaString: String,
    val lastMileTravelString: String? = null
)


@Serializable
data class RestoDetailsResult(
    val restoInfo: RestoDetailsRestoInfo,
    val categoryItems: List<RestoDetailsMenuCategory>
)


object SampleRestoDetails {
    fun parsedResponse(context: Context): RestoDetailsResponse {
        return context.parseDebugJson("sample_data/resto_details_response.json")
    }

    fun sampleRestoDetailsRestoInfo(context: Context): RestoDetailsRestoInfo {
        return try {
            parsedResponse(context)
                .data.cards.getOrNull(2)?.card?.card?.info
                ?: fallbackRestoInfo
        } catch (_: Error) {
            fallbackRestoInfo
        }
    }

    fun sampleFoodItem(context: Context): RestoDetailsCategoryFoodItem {
        return try {
            // Items present within a subcategory
            parsedResponse(context).data.cards.getOrNull(5)
                ?.groupedCard?.cardGroupMap?.regularMenu?.cards?.getOrNull(2)
                ?.card?.card?.subCategories?.getOrNull(1)
                ?.itemCards?.getOrNull(1)?.card?.info
            // Item present directly without subcategory
                ?: parsedResponse(context).data.cards.getOrNull(5)
                    ?.groupedCard?.cardGroupMap?.regularMenu?.cards?.getOrNull(2)
                    ?.card?.card?.itemCards?.getOrNull(1)?.card?.info
                // Default value if null
                ?: fallbackFoodItem
        } catch (_: Error) {
            fallbackFoodItem
        }
    }


    private val fallbackRestoInfo = RestoDetailsRestoInfo(
        id = "732719",
        name = "Oven Story Pizza",
        cloudinaryImageId = "RX_THUMBNAIL/IMAGES/VENDOR/2026/3/5/7cdc93ac-fc55-47f8-a908-ed8bf95cf5fd_732719.JPG",
        cuisines = listOf("Pizzas, Pastas"),
        avgRating = 4.4,
        totalRatingsString = "15K+ ratings",
        city = "Pune",
        locality = "Wakad Road",
        areaName = "Hinjawadi",
        sla = RestoDetailsRestoInfoSla(
            deliveryTime = 29,
            slaString = "25-30 mins"
        ),
    )

    private val fallbackFoodItem = RestoDetailsCategoryFoodItem(
        id = "823617",
        name = "Paneer Punjabi",
        description = "Serves 3 | Must Try! Based on unique recipe of Paneer cooked in creamy onion, tomato gravy with a hint of blanched palak",
        isVeg = 1,
        defaultPrice = 38000,
    )
}