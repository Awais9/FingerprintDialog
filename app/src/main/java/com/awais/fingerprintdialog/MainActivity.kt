package com.awais.fingerprintdialog

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import com.awais.fingerprintdialoglib.Utils.Companion.alertDialog
import com.awais.fingerprintdialoglib.listeners.DialogCallBack
import com.awais.fringerprintdialog.handlers.PrintDialogHelper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), DialogCallBack {

    private val fingerPrintPermission = 9
    private var fingerprintHelper: PrintDialogHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        finger_print_tv.setOnClickListener {
            checkForPermission()
        }
    }

    private fun callFingerPrint() {
        if (fingerprintHelper == null) {
            fingerprintHelper = PrintDialogHelper()
        }
        fingerprintHelper!!.init(this, this)
    }

    private fun checkForPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.USE_FINGERPRINT), fingerPrintPermission)
            alertDialog(this, "Please enable the fingerprint permission")
        } else {
            callFingerPrint()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == fingerPrintPermission) {
                callFingerPrint()
            }
        }
    }

    override fun onCancel(message: String) {
        alertDialog(this, message)
    }

    override fun onSuccess(message: String, result: FingerprintManager.AuthenticationResult) {
        alertDialog(this, message)
    }

    override fun onError(message: String) {
        if (message.contains("too many attempts", true)) {
            alertDialog(this, message)
        }
    }

    override fun onFailed(message: String) {
        if (message.contains("too many attempts", true)) {
            alertDialog(this, message)
        }
    }

    override fun onHelp(message: String) {
    }

}
