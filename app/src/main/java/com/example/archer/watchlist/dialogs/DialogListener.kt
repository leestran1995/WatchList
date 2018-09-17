package com.example.archer.watchlist.dialogs

interface DialogListener {
    fun applyText(title: String)
    fun removeItem(position: Int)
}