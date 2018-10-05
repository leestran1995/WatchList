package com.example.archer.watchlist.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatDialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import com.example.archer.watchlist.Media
import com.example.archer.watchlist.R
import com.example.archer.watchlist.SearchRecyclerViewAdapter
import com.example.archer.watchlist.services.OmdbSearchResponse

class SearchDialog : AppCompatDialogFragment() {
    var inputText: EditText? = null
    var listener: DialogListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val inflater: LayoutInflater? = activity?.layoutInflater
        val view: View? = inflater?.inflate(R.layout.search_dialog, null)


        val searchResponse: ArrayList<Media> = arguments!!.getSerializable("searchResponse") as ArrayList<Media>

        val recyclerView: RecyclerView = view!!.findViewById(R.id.search_dialog_recycler_view)
        val adapter = SearchRecyclerViewAdapter(activity as Context, searchResponse, this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity as Context)
        adapter.notifyDataSetChanged()

        builder.setView(view)
                .setNegativeButton("Cancel") { dialog, whichButton ->
                    // Pass Through
                }
        return builder.create()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        listener = context as DialogListener
    }
}