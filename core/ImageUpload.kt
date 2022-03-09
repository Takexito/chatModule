package com.dev.podo.core

import android.net.Uri
import androidx.fragment.app.Fragment

interface ImageUpload {
    fun getFragment(): Fragment
    fun updateImageData(position: Int, uri: Uri)
    fun showError(message: String)
}