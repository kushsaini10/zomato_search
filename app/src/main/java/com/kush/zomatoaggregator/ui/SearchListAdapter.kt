package com.kush.zomatoaggregator.ui

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.kush.zomatoaggregator.databinding.SearchCuisineItemBinding
import com.kush.zomatoaggregator.databinding.SearchResultItemBinding
import com.kush.zomatoaggregator.network.models.Model
import com.kush.zomatoaggregator.util.GlideApp

class SearchListAdapter(
    private val context: Context, private val searchList: MutableList<Model.SearchListItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            Model.SearchListItem.Type.CUISINE.ordinal -> {
                SearchCuisineViewHolder(
                    SearchCuisineItemBinding.inflate(
                        LayoutInflater.from(context),
                        parent,
                        false
                    ))
            }
            else -> {
                SearchResultViewHolder(
                    SearchResultItemBinding.inflate(
                        LayoutInflater.from(context),
                        parent,
                        false
                    ))
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return searchList[position].itemType.ordinal
    }

    override fun getItemCount(): Int = searchList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SearchResultViewHolder -> {
                holder.binding.tvName.text = searchList[position].name
                searchList[position].imageUrl?.let {
                    GlideApp.with(context)
                        .load(Uri.parse(it))
                        .transform(RoundedCorners(8))
                        .into(holder.binding.ivImage)
                }
            }
            is SearchCuisineViewHolder -> {
                holder.binding.tvName.text = searchList[position].cuisine
            }
        }
    }

    class SearchResultViewHolder(var binding: SearchResultItemBinding):
        RecyclerView.ViewHolder(binding.root)

    class SearchCuisineViewHolder(var binding: SearchCuisineItemBinding):
        RecyclerView.ViewHolder(binding.root)

}