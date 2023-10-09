package com.sameh.pdfreader.extensions

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import java.io.Serializable

fun String.showToast(context: Context) {
    Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
}

fun String.toLogD(tag: String = "myDebuggingTAG") {
    Log.d(tag, "data: $this")
}

fun String.toLogE(tag: String = "myDebuggingTAG") {
    Log.e(tag, "data: $this")
}

inline fun <reified T : java.io.Serializable> Bundle.serializable(key: String): T? =
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializable(
            key,
            T::class.java
        )

        else -> @Suppress("DEPRECATION") getSerializable(key) as? T
    }

inline fun <reified T : Serializable> Intent.serializable(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializableExtra(
        key,
        T::class.java
    )

    else -> @Suppress("DEPRECATION") getSerializableExtra(key) as? T
}