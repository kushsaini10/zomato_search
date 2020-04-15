package com.kush.zomatoaggregator.ui

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.kush.zomatoaggregator.R
import com.kush.zomatoaggregator.databinding.SearchCuisineItemBinding
import com.kush.zomatoaggregator.databinding.SearchResultItemBinding
import com.kush.zomatoaggregator.network.models.Model
import com.kush.zomatoaggregator.util.AdapterSearchItemActionsClickListener
import com.kush.zomatoaggregator.util.GlideApp

class SearchListAdapter(
    private val context: Context,
    private val searchList: MutableList<Model.SearchListItem>,
    private val onActionsClickListener: AdapterSearchItemActionsClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            Model.SearchListItem.Type.CUISINE.ordinal -> {
                SearchCuisineViewHolder(
                    SearchCuisineItemBinding.inflate(
                        LayoutInflater.from(context),
                        parent,
                        false
                    )
                )
            }
            else -> {
                SearchResultViewHolder(
                    SearchResultItemBinding.inflate(
                        LayoutInflater.from(context),
                        parent,
                        false
                    )
                ).apply {
                    this.binding.btnMap.setOnClickListener {
                        onActionsClickListener.onMapClicked(adapterPosition)
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return searchList[position].itemType.ordinal
    }

    override fun getItemCount(): Int = searchList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val searchListItem = searchList[position]
        when (holder) {
            is SearchResultViewHolder -> {
                if (searchListItem.imageUrl.isNullOrBlank()) {
                    GlideApp.with(context)
                        .load(R.drawable.ic_placeholder)
                        .into(holder.binding.ivImage)
                } else {
                    GlideApp.with(context)
                        .load(Uri.parse(searchListItem.imageUrl))
                        .placeholder(R.drawable.ic_placeholder)
                        .transform(RoundedCorners(8))
                        .into(holder.binding.ivImage)
                }
                holder.binding.tvName.text = searchListItem.name
                holder.binding.tvLocality.text =
                    if (!searchListItem.city.isNullOrEmpty())
                        "${searchListItem.locality}, ${searchListItem.city}"
                    else
                        searchListItem.locality
                holder.binding.tvAvgPrice.isVisible =
                    searchListItem.averageCostForTwo != null &&
                            searchListItem.averageCostForTwo != 0
                holder.binding.tvAvgPrice.text = context.getString(
                    R.string.text_restaurant_price,
                    searchListItem.currency,
                    searchListItem.averageCostForTwo
                )
                holder.binding.btnMap.isVisible =
                    !searchListItem.latitude.isNullOrBlank() && !searchListItem.latitude.isNullOrBlank()
            }
            is SearchCuisineViewHolder -> {
                holder.binding.tvName.text = searchListItem.cuisine
            }
        }
    }

    fun setData(newSearchList: MutableList<Model.SearchListItem>) {
        val diffCallback = SearchResultDiffCallback(searchList, newSearchList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        searchList.clear()
        searchList.addAll(newSearchList)
        diffResult.dispatchUpdatesTo(this)
    }

    class SearchResultViewHolder(var binding: SearchResultItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    class SearchCuisineViewHolder(var binding: SearchCuisineItemBinding) :
        RecyclerView.ViewHolder(binding.root)

}