package com.example.archer.watchlist.dialogs

interface DialogListener {
    fun applyNewMediaText(title: String)
    fun removeItem(position: Int)
    fun applyNewChannelText(name: String, status: Int = 0)
    fun removeChannel(title: String?)
}