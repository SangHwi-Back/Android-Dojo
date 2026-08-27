package com.example.moviceapp.common

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.moviceapp.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CommonDialog : DialogFragment() {
    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_MESSAGE = "message"
        private const val ARG_LAYOUT_RESOURCE_ID = "resource_layout_id"

        fun newInstance(title: String, message: String, layout: Int? = null): CommonDialog {
            val dialog = CommonDialog()
            val args = Bundle()
            args.putString(ARG_TITLE, title)
            args.putString(ARG_MESSAGE, message)
            if (layout != null) args.putInt(ARG_LAYOUT_RESOURCE_ID, layout)
            dialog.arguments = args
            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title = arguments?.getString(ARG_TITLE) ?: ""
        val message = arguments?.getString(ARG_MESSAGE) ?: ""

        val builder = MaterialAlertDialogBuilder(requireContext(), R.style.Theme_CineBook_Dialog)

        val view = arguments?.getInt(ARG_LAYOUT_RESOURCE_ID)?.let {
            if (it == 0) null else layoutInflater.inflate(it, null)
        }

        builder.apply {
            setTitle(title)
            setMessage(message)

            if (view != null) setView(view)

            setPositiveButton(android.R.string.ok) { _, _ ->
                dismiss()
            }
            setNegativeButton(android.R.string.cancel) { _, _ ->
                dismiss()
            }
        }

        return builder.create().apply {
            // Dismiss when tapping outside the dialog
            setCanceledOnTouchOutside(true)
        }
    }
}
