package com.example.archer.watchlist

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.bumptech.glide.Glide
import com.example.archer.watchlist.activities.MainActivity
import com.example.archer.watchlist.dataclasses.Media
import com.example.archer.watchlist.dialogs.SearchDialog

import java.util.ArrayList


/**
 * Created by User on 1/1/2018.
 */

class SearchRecyclerViewAdapter(
        private val mContext: Context,
        var mMedia: ArrayList<Media>,
        var mDialog: SearchDialog
        ) : RecyclerView.Adapter<SearchRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_listitem, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        Glide.with(mContext)
                .load(mMedia[position].imageLink)
                .into(holder.image)

        holder.mediaTitle.text = mMedia[position].title
        holder.mediaSummary.text = mMedia[position].summary

        holder.itemView.setOnClickListener() {
            val instance = mContext as MainActivity
            instance.addMedia(mMedia[position])
            mDialog.dismiss()
        }
    }

    override fun getItemCount(): Int = mMedia.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var image: ImageView
        internal var mediaTitle: TextView
        internal var mediaSummary: TextView

        init {
            image = itemView.findViewById(R.id.image)
            mediaTitle = itemView.findViewById(R.id.mediaTitle)
            mediaSummary = itemView.findViewById(R.id.mediaSummary)
        }
    }
}
