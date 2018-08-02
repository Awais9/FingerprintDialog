package com.awais.fingerprintdialoglib

import android.app.AlertDialog
import android.content.Context

class Utils {

    companion object {
        fun alertDialog(context: Context, message: String) {
            val alert = AlertDialog.Builder(context)
            alert.setTitle(R.string.app_name)
            alert.setMessage(message)
            alert.setCancelable(false)
            alert.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }.show()
        }
    }
}