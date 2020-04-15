package com.kush.zomatoaggregator.ui

import android.content.Context
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.content.res.getDrawableOrThrow
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.kush.zomatoaggregator.R
import com.kush.zomatoaggregator.databinding.ActivitySearchBinding
import com.kush.zomatoaggregator.network.NetworkHelper
import com.kush.zomatoaggregator.network.models.Model
import com.kush.zomatoaggregator.util.Utils
import com.kush.zomatoaggregator.util.showToast
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var viewModelFactory: SearchViewModelProviderFactory
    private lateinit var viewModel: SearchViewModel
    private lateinit var disposable: CompositeDisposable
    private lateinit var networkHelper: NetworkHelper
    private var searchList: MutableList<Model.SearchListItem> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initVariables()
        initViews()
        initViewModel()
        initObservables()
    }

    override fun onDestroy() {
        disposable?.clear()
        super.onDestroy()
    }

    private fun initVariables() {
        disposable = CompositeDisposable()
        networkHelper = NetworkHelper(applicationContext)
    }

    private fun initViews() {
        initThemeButton()
        initSearch()
        initList()
    }

    private fun initViewModel() {
        viewModelFactory = SearchViewModelProviderFactory(disposable, networkHelper)
        viewModel = ViewModelProvider(this, viewModelFactory).get(SearchViewModel::class.java)
        viewModel.onCreate()
    }

    private fun initObservables() {
        viewModel.isSearching.observe(this, Observer {
            it?.let {
                if (it) {
                    binding.tilSearch.endIconMode = TextInputLayout.END_ICON_CUSTOM
                    binding.tilSearch.endIconDrawable = getProgressBarDrawable()
                    (binding.tilSearch.endIconDrawable as? Animatable)?.start()
                } else {
                    (binding.tilSearch.endIconDrawable as? Animatable)?.stop()
                    binding.tilSearch.endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
                    binding.tilSearch.endIconDrawable = ContextCompat.getDrawable(this, R.drawable.ic_clear_black_24dp)
                }
            }
        })
        viewModel.searchResultList.observe(this, Observer {
            it?.let {
                searchList.clear()
                searchList.addAll(it)
                binding.rvSearch.adapter?.notifyDataSetChanged()
            }
        })
        viewModel.errorMessage.observe(this, Observer {
            it?.let {
                showToast(getString(it))
            }
        })
    }

    private fun initThemeButton() {
        binding.cbTheme.apply {
            isChecked = Utils.isDarkTheme(this@SearchActivity)
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
            layoutManager = LinearLayoutManager(this@SearchActivity, RecyclerView.VERTICAL, false)
            adapter = SearchListAdapter(this@SearchActivity, searchList)
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
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                viewModel.onSearchQueryChange(result)
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

    private fun processError(message: String?) {
        val errorMessage: String = if (message.isNullOrBlank()) {
            getString(R.string.text_common_processing_error)
        } else {
            message
        }
        showToast(message = errorMessage)
    }

}
