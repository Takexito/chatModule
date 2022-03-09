package com.dev.podo.core.model.holder

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import com.dev.podo.R

data class TextWithImageHolder(
    val text: String = "",
    val imageId: Int = R.drawable.ic_transparent
) {
    fun getImage(context: Context): Drawable? {
        return AppCompatResources.getDrawable(context, imageId)
    }

    fun getImage(context: Context, defaultImageId: Int): Drawable? {
        return AppCompatResources.getDrawable(context, imageId) ?: AppCompatResources.getDrawable(
            context,
            defaultImageId
        )
    }
}
