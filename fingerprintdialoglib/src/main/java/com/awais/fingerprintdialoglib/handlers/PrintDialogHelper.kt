package com.awais.fringerprintdialog.handlers

import android.app.KeyguardManager
import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.Handler
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.support.annotation.RequiresApi
import android.support.design.widget.BottomSheetDialog
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import com.awais.fingerprintdialoglib.R
import com.awais.fingerprintdialoglib.Utils.Companion.alertDialog
import com.awais.fingerprintdialoglib.listeners.DialogCallBack
import com.awais.fingerprintdialoglib.listeners.FingerprintInterface
import kotlinx.android.synthetic.main.finger_print_dialog_view.view.*
import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey

class PrintDialogHelper : FingerprintInterface {

    private val KEY_NAME = "guard_key"
    private var cipher: Cipher? = null
    private var keyStore: KeyStore? = null
    private var keyGenerator: KeyGenerator? = null
    private var cryptoObject: FingerprintManager.CryptoObject? = null
    private var fingerprintManager: FingerprintManager? = null
    private var keyguardManager: KeyguardManager? = null
    private lateinit var mView: View
    private lateinit var messageTV: TextView
    private lateinit var sheetDialog: BottomSheetDialog
    private lateinit var fingerPrintHelper: FingerprintHandler
    private lateinit var context: Context
    private lateinit var dialogCallBack: DialogCallBack

    fun init(context: Context, callBack: DialogCallBack) {
        this.context = context
        mView = LayoutInflater.from(context).inflate(R.layout.finger_print_dialog_view, null)
        messageTV = mView.message_tv
        sheetDialog = BottomSheetDialog(context)
        sheetDialog.setContentView(mView)
        sheetDialog.setCancelable(false)
        dialogCallBack = callBack
        setUpKeyGuard()
    }

    private fun setUpKeyGuard() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            keyguardManager = context.getSystemService(AppCompatActivity.KEYGUARD_SERVICE) as KeyguardManager
            fingerprintManager = context.getSystemService(AppCompatActivity.FINGERPRINT_SERVICE) as FingerprintManager

            //Check for fingerprint sensor//
            if (!fingerprintManager!!.isHardwareDetected) {
                alertDialog(context, "Your device doesn't support fingerprint authentication")
            }
            //check for keyguard security
            else if (!keyguardManager!!.isKeyguardSecure) {
                alertDialog(context, "Lock screen security not enabled in Settings")
            }
            //Check for stored fingerprint//
            else if (!fingerprintManager!!.hasEnrolledFingerprints()) {
                alertDialog(context, "No fingerprint configured. Please register at least one fingerprint in your device's Settings")
            } else {
                try {
                    generateKey()
                } catch (e: Exception) {
                    Log.e("generateKey()", e.toString())
                }

                if (initCipher()) {
                    cryptoObject = FingerprintManager.CryptoObject(cipher)
                    fingerPrintHelper = FingerprintHandler(context, this)
                    fingerPrintHelper.startAuth(fingerprintManager!!, cryptoObject!!)
                    fingerPrintDialog()
                }
            }
        } else {
            alertDialog(context, "Your operating system does not support fingerprint authentication")
        }
    }

    @Throws(Exception::class)
    private fun generateKey() {
        try {
            // Obtain a reference to the Keystore using the standard Android keystore container identifier (“AndroidKeystore”)//
            keyStore = KeyStore.getInstance("AndroidKeyStore")

            //Generate the key//
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")

            //Initialize an empty KeyStore//
            keyStore!!.load(null)

            //Initialize the KeyGenerator//
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                keyGenerator!!.init(
                        //Specify the operation(s) this key can be used for//
                        KeyGenParameterSpec.Builder(KEY_NAME,
                                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)

                                //Configure this key so that the user has to confirm their identity with a fingerprint each time they want to use it//
                                .setUserAuthenticationRequired(true)
                                .setEncryptionPaddings(
                                        KeyProperties.ENCRYPTION_PADDING_PKCS7)
                                .build())
            } else {
                alertDialog(context, "Please update your os to Marshmallow")
                return
            }

            //Generate the key//
            keyGenerator!!.generateKey()

        } catch (exc: KeyStoreException) {
            exc.printStackTrace()
            throw Exception(exc)
        } catch (exc: NoSuchAlgorithmException) {
            exc.printStackTrace()
            throw Exception(exc)
        } catch (exc: NoSuchProviderException) {
            exc.printStackTrace()
            throw Exception(exc)
        } catch (exc: InvalidAlgorithmParameterException) {
            exc.printStackTrace()
            throw Exception(exc)
        } catch (exc: CertificateException) {
            exc.printStackTrace()
            throw Exception(exc)
        } catch (exc: IOException) {
            exc.printStackTrace()
            throw Exception(exc)
        }

    }

    private fun initCipher(): Boolean {
        try {
            //Obtain a cipher instance and configure it with the properties required for fingerprint authentication//
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to get Cipher", e)
        } catch (e: NoSuchPaddingException) {
            throw RuntimeException("Failed to get Cipher", e)
        }

        try {
            keyStore!!.load(
                    null)
            val key = keyStore!!.getKey(KEY_NAME, null) as SecretKey
            cipher!!.init(Cipher.ENCRYPT_MODE, key)
            //Return true if the cipher has been initialized successfully//
            return true
        } catch (e: Exception) {
            //Return false if cipher initialization failed//
            return false
        } catch (e: KeyStoreException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: CertificateException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: UnrecoverableKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: IOException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: InvalidKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun fingerPrintDialog() {
        sheetDialog.show()
        mView.cancel_tv.setOnClickListener {
            sheetDialog.dismiss()
            fingerPrintHelper.stopAuth()
        }
        startAnimatemessageTV()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCancel(message: String) {
        dialogCallBack.onCancel(message)
//        fingerPrintHelper.stopAuth()
        dismissSheet()
    }

    override fun onSuccess(message: String, result: FingerprintManager.AuthenticationResult) {
        dialogCallBack.onSuccess(message, result)
        dismissSheet()
    }

    override fun onError(message: String) {
        dialogCallBack.onError(message)
        if (message.contains("too many attempts", true)) {
            dismissSheet()
        } else {
            messageTV.text = message
            messageTV.setTextColor(ContextCompat.getColor(context, R.color.error))
            startAnimatemessageTV()
        }
    }

    override fun onFailed(message: String) {
        dialogCallBack.onFailed(message)
        if (message.contains("too many attempts", true)) {
            dismissSheet()
        } else {
            messageTV.text = message
            messageTV.setTextColor(ContextCompat.getColor(context, R.color.error))
            startAnimatemessageTV()
        }
    }

    override fun onHelp(message: String) {
        dialogCallBack.onHelp(message)
        messageTV.text = message
        messageTV.setTextColor(ContextCompat.getColor(context, R.color.help))
        Thread.sleep(5000)
        startAnimatemessageTV()
    }

    private fun dismissSheet() {
        messageTV.text = ""
        stopAnimatemessageTV()
        sheetDialog.dismiss()
    }

    private fun startAnimatemessageTV() {
        val handler = Handler()
        handler.postDelayed({
            messageTV.setTextColor(ContextCompat.getColor(context, R.color.help))
            messageTV.text = "Identifying..."
            val animation = AnimationUtils.loadAnimation(context, R.anim.message_blink)
            messageTV.startAnimation(animation)
        }, 2000)
    }

    private fun stopAnimatemessageTV() {
        messageTV.clearAnimation()
    }

}