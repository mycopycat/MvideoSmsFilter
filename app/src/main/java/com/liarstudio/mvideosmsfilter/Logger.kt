package com.liarstudio.mvideosmsfilter

import android.util.Log

object Logger {
    fun log(obj: Any) {
        val stack = Thread.currentThread().stackTrace

        val stackObject = stack.find {t -> t.className == obj.javaClass.canonicalName }
        Log.d(obj.javaClass.simpleName, stackObject!!.methodName)
    }
}