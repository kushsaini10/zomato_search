package com.kush.zomatoaggregator.util

import androidx.appcompat.app.AppCompatActivity

fun AppCompatActivity.showToast(message: String) {
    if (!isDestroyed) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}
