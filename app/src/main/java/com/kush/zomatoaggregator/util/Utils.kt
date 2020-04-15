package com.kush.zomatoaggregator.util

import android.content.Context
import android.content.res.Configuration

/**
 * Created by Kush Saini on 15/4/20
 * Last Modified by Kush Saini on 15/4/20
 * Description:
 */
class Utils {
    companion object{
        fun isDarkTheme(context: Context): Boolean {
            return (context.resources.configuration.uiMode
                    and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        }
    }
}