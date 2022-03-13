package com.qoohoosen.utils

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.material.snackbar.Snackbar
import java.io.*


object Utilities {
    @JvmStatic
    fun hideKeyPad(context: Context, view: View) {
        try {
            val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        } catch (e: Exception) {
            if (e != null) e.printStackTrace()
        }
    }


    fun fileToBytes(file: File): ByteArray? {
        val size = file.length() as Int
        val bytes = ByteArray(size)
        try {
            val buf = BufferedInputStream(FileInputStream(file))
            buf.read(bytes, 0, bytes.size)
            buf.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bytes
    }

    @JvmStatic
    fun showSnackBar(activity: Activity, view: View, message: String) {

        val snackbar: Snackbar = Snackbar.make(
            activity.getWindow().getDecorView().getRootView(),
            message,
            Snackbar.LENGTH_SHORT
        )
        val snackBarView = snackbar.view
        snackBarView.translationY = -convertDpToPixel(96, activity)
        snackbar.show()

//        val snackbar = Snackbar
//            .make(view, msg, Snackbar.LENGTH_LONG)
//        snackbar.show()

    }

    fun convertDpToPixel(dp: Int, context: Context): Float {
        return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

}