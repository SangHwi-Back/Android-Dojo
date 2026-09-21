package com.example.moviceapp.myinfo

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.moviceapp.databinding.DialogEditProfilePhotoBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ProfileImageDialogFragment : DialogFragment() {

    private var _binding: DialogEditProfilePhotoBinding? = null
    private val binding get() = _binding!!
    private val result = MutableStateFlow<ProfileImageResult?>(null)
    private val launcherTakePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap == null)
            result.value = null
        else
            ProfileImageResult.ResultBitmap(bitmap).let {
                result.value = it
                setPreviewImage(it)
            }
    }
    private val launcherPickGalleryImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri?->
        if (uri == null)
            result.value = null
        else
            ProfileImageResult.ResultUri(uri).let {
                result.value = it
                setPreviewImage(it)
            }
    }
    interface Listener {
        fun onConfirm(result: ProfileImageResult)
    }

    var listener: Listener? = null

    companion object {
        fun newInstance(listener: Listener? = null): ProfileImageDialogFragment {
            val fragment = ProfileImageDialogFragment()
            fragment.listener = listener
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogEditProfilePhotoBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()

        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        lifecycleScope.launch {
            result.collect {
                binding.confirmButton.visibility = if (it == null) View.GONE else View.VISIBLE
            }
        }

        binding.buttonClose.setOnClickListener {
            dismiss()
        }

        binding.buttonTakePhoto.setOnClickListener {
            // Launch camera intent
            launcherTakePhoto.launch(null)
        }

        binding.buttonPickGallery.setOnClickListener {
            // Launch gallery intent
            val request = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            launcherPickGalleryImage.launch(request)
        }

        binding.confirmButton.setOnClickListener {
            result.value?.let { listener?.onConfirm(it) }
            dismiss()
        }

        return dialog
    }

    fun setPreviewImage(result: ProfileImageResult?) {
        binding.profileImagePreview.apply {
            when (result) {
                is ProfileImageResult.ResultBitmap -> setImageBitmap(result.bitmap)
                is ProfileImageResult.ResultUri -> setImageURI(result.uri)
                else -> setImageURI(null)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    sealed class ProfileImageResult {
        data class ResultBitmap(val bitmap: Bitmap) : ProfileImageResult()
        data class ResultUri(val uri: Uri) : ProfileImageResult()
    }
}