package com.example.archer.watchlist.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatDialogFragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.example.archer.watchlist.R

class DeleteChannelDialog : AppCompatDialogFragment() {
    var listener: DialogListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val inflater: LayoutInflater? = activity?.layoutInflater
        val view: View? = inflater?.inflate(R.layout.layout_dialog_delete_channel, null)

        builder.setView(view)
                .setNegativeButton("Cancel") { dialog, whichButton ->
                    // Pass Through
                }
                .setPositiveButton("Ok") { dialog, whichButton ->
                    listener!!.removeChannel(arguments!!.getString("title"))
                }
        return builder.create()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        listener = context as DialogListener
    }

}