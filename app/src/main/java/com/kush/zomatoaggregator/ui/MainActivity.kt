package com.kush.zomatoaggregator.ui

import android.content.Context
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.content.res.getDrawableOrThrow
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.kush.zomatoaggregator.R
import com.kush.zomatoaggregator.databinding.ActivityMainBinding
import com.kush.zomatoaggregator.network.NetworkHelper
import com.kush.zomatoaggregator.network.NetworkService
import com.kush.zomatoaggregator.network.models.Model
import com.kush.zomatoaggregator.util.Utils
import com.kush.zomatoaggregator.util.showToast
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    companion object{
        private const val otherCuisine = "Other"
    }

    private lateinit var binding: ActivityMainBinding
    private var disposable: CompositeDisposable? = null
    private lateinit var networkHelper: NetworkHelper
    private var searchList: MutableList<Model.SearchListItem> = mutableListOf()
    private lateinit var listHashMap: HashMap<String, HashSet<Model.SearchListItem>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initVariables()
        initViews()
    }

    override fun onDestroy() {
        disposable?.clear()
        super.onDestroy()
    }

    private fun initVariables() {
        disposable = CompositeDisposable()
        networkHelper = NetworkHelper(this)
        listHashMap = hashMapOf()
    }

    private fun initViews() {
        initThemeButton()
        initSearch()
        initList()
    }

    private fun initThemeButton() {
        binding.cbTheme.apply {
            isChecked = Utils.isDarkTheme(this@MainActivity)
            setOnCheckedChangeListener { _, isChecked ->
                AppCompatDelegate.setDefaultNightMode(
                    if (isChecked)
                        AppCompatDelegate.MODE_NIGHT_YES
                    else
                        AppCompatDelegate.MODE_NIGHT_NO
                )
                delegate.applyDayNight()
            }
        }
    }

    private fun initList() {
        binding.rvSearch.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, RecyclerView.VERTICAL, false)
            adapter = SearchListAdapter(this@MainActivity, searchList)
        }
        binding.btnTop.setOnClickListener {
            binding.rvSearch.smoothScrollToPosition(0)
        }
    }

    private fun initSearch() {
        binding.tilSearch.endIconDrawable = getProgressBarDrawable()
        binding.tilSearch.setEndIconTintList(ContextCompat.getColorStateList(this, R.color.color_search_end_icon_state))

        val eventsSearchDisposable = createTextChangeObservable()
            .debounce(300, TimeUnit.MILLISECONDS)
            .filter { query -> ((query.isNotBlank()) || query.isEmpty()) }
            .distinctUntilChanged()
            .switchMap { query ->
                searchEventList(query)?.subscribeOn(Schedulers.io())
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                processSearchResponse(result)
            }, { throwable ->
                throwable.printStackTrace()
                processError(null)
            })

        disposable?.add(eventsSearchDisposable)
    }

    private fun Context.getProgressBarDrawable(): Drawable {
        val value = TypedValue()
        theme.resolveAttribute(android.R.attr.progressBarStyleSmall, value, false)
        val progressBarStyle = value.data
        val attributes = intArrayOf(android.R.attr.indeterminateDrawable)
        val array = obtainStyledAttributes(progressBarStyle, attributes)
        val drawable = array.getDrawableOrThrow(0)
        array.recycle()
        drawable.setTintList(ContextCompat.getColorStateList(this, R.color.color_search_end_icon_state))
        return drawable
    }

    private fun processSearchResponse(response: Model.SearchResponse?) {
        (binding.tilSearch.endIconDrawable as? Animatable)?.stop()
        binding.tilSearch.endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
        binding.tilSearch.endIconDrawable = ContextCompat.getDrawable(this, R.drawable.ic_clear_black_24dp)
        response?.let {
            searchList.clear()
            listHashMap.clear()
            it.restaurants?.forEach { restaurantsItem ->
                Log.d("TAG", "${restaurantsItem?.restaurant?.name} : ${restaurantsItem?.restaurant?.id}")
                restaurantsItem?.restaurant?.let {restaurantData ->
                    val parsedCuisines = addUniqueCuisines(restaurantData.cuisines)
                    addRestaurantToCuisineGroup(restaurantData, parsedCuisines)
                }
            }
            listHashMap.entries.forEach { entry ->
                searchList.add(Model.SearchListItem(itemType = Model.SearchListItem.Type.CUISINE, cuisine = entry.key))
                searchList.addAll(entry.value)
            }
        }
        binding.rvSearch.adapter?.notifyDataSetChanged()
    }

    private fun addUniqueCuisines(cuisines: String?): List<String>? {
        cuisines?.let {
            return it.split(",")
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
                imageUrl = restaurantData.thumb
            )
        )
    }

    private fun createTextChangeObservable(): Observable<String> {
        return Observable.create { emitter ->
            val textWatcher = object : TextWatcher {

                override fun afterTextChanged(s: Editable?) = Unit

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

                override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    s?.toString()?.let {
                        emitter.onNext(it)
                    }
                }
            }
            binding.etSearch.addTextChangedListener(textWatcher)
            emitter.setCancellable {
                binding.etSearch.removeTextChangedListener(textWatcher)
            }
        }
    }

    private fun searchEventList(searchText : String): Observable<Model.SearchResponse>? {
        return if (networkHelper.isNetworkConnected()) {
            runOnUiThread {
                binding.tilSearch.endIconMode = TextInputLayout.END_ICON_CUSTOM
                binding.tilSearch.endIconDrawable = getProgressBarDrawable()
                (binding.tilSearch.endIconDrawable as? Animatable)?.start()
            }
            NetworkService.instance.search(query = searchText)
        } else {
            getString(R.string.text_common_processing_error)
            null
        }
    }

    private fun processError(message: String?) {
        val errorMessage: String = if (message.isNullOrBlank()) {
            getString(R.string.text_common_processing_error)
        } else {
            message
        }
        showToast(message = errorMessage)
    }

}
