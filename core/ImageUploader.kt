package com.dev.podo.core

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.dev.podo.R
import com.dev.podo.common.utils.contracts.PositionedContractData
import com.dev.podo.common.utils.contracts.PositionedGetContent
import com.dev.podo.common.utils.contracts.PositionedRequestPermission
import com.dev.podo.common.utils.contracts.PositionedTakePhotoContract
import com.dev.podo.common.utils.files.ImageFileHelper

class ImageUploader(private val imageUpload: ImageUpload) {
    private val fragment: Fragment
        get() = imageUpload.getFragment()

    private fun updateImageWithLifecycle(position: Int, uri: Uri){
        val isFragmentReady = fragment.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        val isFragmentDestroyed = fragment.lifecycle.currentState == Lifecycle.State.DESTROYED

        if (isFragmentReady && !isFragmentDestroyed){
            imageUpload.updateImageData(position, uri)
        }

    }

    private var photoTakeResultLauncher: ActivityResultLauncher<PositionedContractData<Uri>> =
        fragment.registerForActivityResult(PositionedTakePhotoContract()) { photoData ->
            photoData?.let {
                if (photoData.isSuccessful) {
                    photoData.inputData?.let { uri ->
                        updateImageWithLifecycle(photoData.position, uri)
                    }
                    return@registerForActivityResult
                }
                imageUpload.showError(fragment.getString(R.string.error_on_take_photo))
            }
        }
    private var openGalleryResultLauncher: ActivityResultLauncher<PositionedContractData<String>> =
        fragment.registerForActivityResult(
            PositionedGetContent()
        ) { resultData ->
            resultData?.let {
                if (resultData.isSuccessful) {
                    resultData.inputData?.let {
                        imageUpload.updateImageData(resultData.position, it)
                    }
                    return@registerForActivityResult
                }
                imageUpload.showError(fragment.getString(R.string.error_on_gallery_photo))
            }
        }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            fragment.requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun launchTakePhotoIntent(position: Int) {
        val imageUri = ImageFileHelper.createImageFile(fragment.requireActivity())
        val takePhotoInputData = PositionedContractData(
            position,
            inputData = imageUri
        )
        photoTakeResultLauncher.launch(takePhotoInputData)
    }

    private val requestPermissionLauncher = fragment.registerForActivityResult(
        PositionedRequestPermission()
    ) { result: PositionedContractData<*> ->
        if (result.isSuccessful) {
            takePhoto(result.position)
        }
    }

    fun takePhoto(position: Int) {
        if (checkPermission()) {
            launchTakePhotoIntent(position)
        } else {
            requestPermissionLauncher.launch(
                PositionedContractData(
                    position,
                    Manifest.permission.CAMERA
                )
            )
        }
    }

    fun openGallery(position: Int) {
        openGalleryResultLauncher.launch(
            PositionedContractData(
                position,
                "image/*"
            )
        )
    }
}