package com.kush.zomatoaggregator.network

import android.content.Context
import android.net.ConnectivityManager

class NetworkHelper(private val context: Context) {
    companion object {
        private const val TAG = "NetworkHelper"
    }

    fun isNetworkConnected(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        return activeNetwork?.isConnected ?: false
    }

}