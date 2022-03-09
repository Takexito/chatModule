package com.dev.podo.core.ui

import android.widget.Toast
import androidx.fragment.app.Fragment
import com.dev.podo.R
import com.dev.podo.common.ui.CustomDialogFragment
import com.dev.podo.common.utils.ModalViewHelper
import com.dev.podo.common.utils.exceptions.ResourceException
import io.sentry.Sentry
import java.lang.Exception

open class BaseFragment(val view: Int) : Fragment(view) {

    fun showErrorAlert(message: String?, title: String? = null) {
        message?.let { Sentry.addBreadcrumb(message) }
        val alert = CustomDialogFragment(
            title = title ?: getString(R.string.error),
            message = message
        )
        Sentry.captureException(Exception(message))
        ModalViewHelper.safelyShow(
            alert,
            childFragmentManager,
            "ERROR_ALERT"
        )
    }

    fun showErrorAlert(exception: Exception) {
        var message = exception.message

        if (exception is ResourceException) {
            message = resources.getString(exception.stringId)
        }
        message?.let { Sentry.addBreadcrumb(message) }
        Sentry.captureException(exception)
        val alert = CustomDialogFragment(
            title = getString(R.string.error),
            message = message
        )
        ModalViewHelper.safelyShow(
            alert,
            childFragmentManager,
            "ERROR_ALERT"
        )
    }

    fun showToast(message: String, lengh: Int = Toast.LENGTH_LONG) {
        Toast.makeText(requireContext(), message, lengh).show()
    }
}
