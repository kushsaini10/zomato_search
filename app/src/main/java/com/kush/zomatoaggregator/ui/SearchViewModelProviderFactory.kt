package com.kush.zomatoaggregator.ui


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kush.zomatoaggregator.network.NetworkHelper
import io.reactivex.disposables.CompositeDisposable

class SearchViewModelProviderFactory(
    private val compositeDisposable: CompositeDisposable,
    private val networkHelper: NetworkHelper
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalArgumentException::class)
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            return SearchViewModel(compositeDisposable, networkHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
