package com.example.archer.watchlist

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import com.bumptech.glide.Glide
import com.example.archer.watchlist.constants.DELETE_RECYCLER_ENTRY

import java.util.ArrayList


/**
 * Created by User on 1/1/2018.
 */

class RecyclerViewAdapter(
        private val mContext: Context,
        var mMedia: ArrayList<Media>
) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

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

        holder.itemView.setOnLongClickListener {
            val instance = mContext as MainActivity
            instance.openDeleteMediaDialog(position)
            return@setOnLongClickListener true
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
