package com.awais.fingerprintdialoglib.listeners

import android.hardware.fingerprint.FingerprintManager

interface FingerprintInterface {
    fun onCancel(message: String)
    fun onSuccess(message: String, result: FingerprintManager.AuthenticationResult)
    fun onError(message: String)
    fun onFailed(message: String)
    fun onHelp(message: String)
}