package com.kush.zomatoaggregator.network.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

object Model {
    data class SearchResponse(

        @Expose
        @SerializedName("results_found")
        val resultsFound: Int? = null,

        @Expose
        @SerializedName("results_shown")
        val resultsShown: Int? = null,

        @Expose
        @SerializedName("restaurants")
        val restaurants: List<RestaurantsItem?>? = null,

        @Expose
        @SerializedName("results_start")
        val resultsStart: Int? = null
    ) {
        data class RestaurantsItem(

            @Expose
            @SerializedName("restaurant")
            val restaurant: Restaurant? = null
        )
        data class Restaurant(

            @Expose
            @SerializedName("phone_numbers")
            val phoneNumbers: String? = null,

            @Expose
            @SerializedName("thumb")
            val thumb: String? = null,

            @Expose
            @SerializedName("average_cost_for_two")
            val averageCostForTwo: Int? = null,

            @Expose
            @SerializedName("name")
            val name: String? = null,

            @Expose
            @SerializedName("location")
            val location: Location? = null,

            @Expose
            @SerializedName("currency")
            val currency: String? = null,

            @Expose
            @SerializedName("id")
            val id: String? = null,

            @Expose
            @SerializedName("cuisines")
            val cuisines: String? = null
        )
        data class Location(

            @Expose
            @SerializedName("latitude")
            val latitude: String? = null,

            @Expose
            @SerializedName("locality")
            val locality: String? = null,

            @Expose
            @SerializedName("city")
            val city: String? = null,

            @Expose
            @SerializedName("longitude")
            val longitude: String? = null
        )
    }
    data class SearchListItem(
        val itemType: Type = Type.RESTAURANT,
        val id: String? = null,
        val name: String = "",
        val cuisine: String = "",
        val averageCostForTwo: Int? = null,
        val currency: String? = null,
        val imageUrl: String? = null,
        val locality: String? = null,
        val city: String? = null,
        val latitude: String? = null,
        val longitude: String? = null
    ) {
        enum class Type {
            CUISINE, RESTAURANT
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other is SearchListItem) {
                return id == other.id && cuisine == other.cuisine && name == other.name
            }
            return super.equals(other)
        }

        override fun hashCode(): Int {
            return Objects.hash(id, name, cuisine)
        }
    }
}
