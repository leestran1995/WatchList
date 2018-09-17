package com.example.archer.watchlist.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import com.example.archer.watchlist.R

class TitleDialog : AppCompatDialogFragment() {
    var inputText: EditText? = null
    var listener: DialogListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val inflater: LayoutInflater? = activity?.layoutInflater
        val view: View? = inflater?.inflate(R.layout.layout_dialog, null)
        builder.setView(view)
                .setNegativeButton("Cancel") { dialog, whichButton ->

                }
                .setPositiveButton("Submit") { dialog, whichButton ->
                    val inputTitle: String? = inputText?.text.toString()
                    if (inputTitle != null) {
                        listener?.applyText(inputTitle)
                    }
                }
        inputText = view?.findViewById(R.id.edit_media_input)
        return builder.create()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        listener = context as DialogListener
    }

}