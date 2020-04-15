package com.kush.zomatoaggregator.ui

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kush.zomatoaggregator.R
import com.kush.zomatoaggregator.network.NetworkHelper
import com.kush.zomatoaggregator.network.NetworkService
import com.kush.zomatoaggregator.network.models.Model
import com.kush.zomatoaggregator.util.SingleLiveEvent
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


class SearchViewModel(
    private val compositeDisposable: CompositeDisposable,
    private val networkHelper: NetworkHelper
) : ViewModel() {

    companion object {
        private const val otherCuisine = "Other"
    }

    val searchQuery: MutableLiveData<String> = MutableLiveData()
    val isSearching: MutableLiveData<Boolean> = MutableLiveData()
    val errorMessage: SingleLiveEvent<Int> = SingleLiveEvent()
    val searchResultList: MutableLiveData<MutableList<Model.SearchListItem>> = MutableLiveData()
    private lateinit var listHashMap: HashMap<String, HashSet<Model.SearchListItem>>

    fun onCreate() {
        listHashMap = hashMapOf()
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.postValue(query)
        searchRestaurantsList(query)?.let {
            isSearching.value = true
            it.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ result ->
                    processSearchResponse(result)
                }, { throwable ->
                    throwable.printStackTrace()
                    errorMessage.value = R.string.text_common_processing_error
                    isSearching.value = false
                })
        }
    }

    private fun searchRestaurantsList(searchText: String): Observable<Model.SearchResponse>? {
        return if (networkHelper.isNetworkConnected()) {
            NetworkService.instance.search(query = searchText)
        } else {
            errorMessage.value = R.string.text_error_no_internet
            null
        }
    }

    private fun processSearchResponse(response: Model.SearchResponse?) {
        response?.let {
            val searchList: MutableList<Model.SearchListItem> = mutableListOf()
            listHashMap.clear()
            it.restaurants?.forEach { restaurantsItem ->
                restaurantsItem?.restaurant?.let { restaurantData ->
                    val parsedCuisines = addUniqueCuisines(restaurantData.cuisines)
                    addRestaurantToCuisineGroup(restaurantData, parsedCuisines)
                }
            }
            listHashMap.entries.forEach { entry ->
                searchList.add(
                    Model.SearchListItem(
                        itemType = Model.SearchListItem.Type.CUISINE,
                        cuisine = entry.key
                    )
                )
                searchList.addAll(entry.value)
            }
            searchResultList.value = searchList
        }
        isSearching.value = false

    }

    private fun addUniqueCuisines(cuisines: String?): List<String>? {
        cuisines?.let {
            return it.split(",").map {cuisine ->
                cuisine.trim()
            }
        }
        return null
    }

    private fun addRestaurantToCuisineGroup(
        restaurantData: Model.SearchResponse.Restaurant,
        parsedCuisines: List<String>?
    ) {
        if (parsedCuisines.isNullOrEmpty()) {
            addCuisineAndRestaurant(otherCuisine, restaurantData)
        } else {
            parsedCuisines.forEach { cuisine ->
                addCuisineAndRestaurant(cuisine, restaurantData)
            }
        }
    }

    private fun addCuisineAndRestaurant(
        cuisine: String,
        restaurantData: Model.SearchResponse.Restaurant
    ) {
        val restaurantHashSet =
            listHashMap[cuisine]
        if (restaurantHashSet == null) {
            listHashMap[cuisine] = hashSetOf()
        }
        listHashMap[cuisine]?.add(
            Model.SearchListItem(
                itemType = Model.SearchListItem.Type.RESTAURANT,
                id = restaurantData.id,
                name = restaurantData.name ?: "",
                cuisine = cuisine,
                imageUrl = restaurantData.thumb,
                averageCostForTwo = restaurantData.averageCostForTwo,
                currency = restaurantData.currency,
                locality = restaurantData.location?.locality,
                city = restaurantData.location?.city,
                latitude = restaurantData.location?.latitude,
                longitude = restaurantData.location?.longitude
            )
        )
    }

}