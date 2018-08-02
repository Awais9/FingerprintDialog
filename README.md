Android Fingerprint Dialog
By this you can get the fingerprint authentication and can use it from any activity.
Note: All code is writen in Kotlin

Add as dependency 

Add following into your main project gradle file

    allprojects {
        repositories {
            maven { url 'https://jitpack.io' }
        }
    }

And now add dependency for library in your app gradle file

    implementation 'com.github.Awais9:FingerprintDialog:v1.2'

How to use Fingerprint dialog library?

Fingerprint dialog library uses Marshmalow and above than 23 SDK versions. As it uses latest android versions you have to check run time permissions for fingerprint sensor. First add these permissions in Android Manifest file:

    <uses-permission android:name="android.permission.USE_FINGERPRINT" />

    <uses-feature
        android:name="android.hardware.fingerprint"
        android:required="false" />
        
Now check on runtime in your activity

    private fun checkForPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED){
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
    
Now you just have to call checkForPermission method with any button where it is required then

    private fun callFingerPrint() {
        if (fingerprintHelper == null) {
            fingerprintHelper = PrintDialogHelper()
        }
        fingerprintHelper!!.init(this, this)
    }
This function is use to handle all the fingerprint events and if you want to get the callbacks in your calling activity then just implement DialogCallBack interface with your activity and it will provide all the values in main.

Please feel free to use and contribute the library. 







