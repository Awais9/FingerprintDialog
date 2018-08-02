package com.awais.fringerprintdialog.handlers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.CancellationSignal
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import com.awais.fingerprintdialoglib.listeners.FingerprintInterface

/*
* Created by Awais Munawar
* August 01, 2018
* */

@RequiresApi(Build.VERSION_CODES.M)
class FingerprintHandler(private val context: Context, private var listener:
FingerprintInterface) : FingerprintManager.AuthenticationCallback() {

    private var cancellationSignal: CancellationSignal? = null

    fun startAuth(manager: FingerprintManager, cryptoObject: FingerprintManager.CryptoObject) {
        cancellationSignal = CancellationSignal()
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null)
    }

    fun stopAuth() {
//        cancellationSignal!!.cancel()
        listener.onCancel("Authentication is cancelled")
    }

    override
    fun onAuthenticationError(errMsgId: Int, errString: CharSequence) {
        listener.onError("Authentication error\n$errString")
    }

    override
    fun onAuthenticationFailed() {
        listener.onFailed("Authentication failed\nTry again")
    }

    override
    fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence) {
        listener.onHelp("Authentication help\n$helpString")
    }

    override
    fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
        listener.onSuccess("Success", result)
    }
}