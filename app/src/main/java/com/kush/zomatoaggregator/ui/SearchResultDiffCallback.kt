package com.kush.zomatoaggregator.ui

import androidx.recyclerview.widget.DiffUtil
import com.kush.zomatoaggregator.network.models.Model

class SearchResultDiffCallback(private val oldList: List<Model.SearchListItem>, private val newList: List<Model.SearchListItem>) :
    DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
        val (_, id, name, cuisine) = oldList[oldPosition]
        val (_, id1, name1, cuisine1) = newList[newPosition]
        return name == name1 && cuisine == cuisine1
    }


}