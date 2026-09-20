package com.example.moviceapp.common

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.example.moviceapp.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.Serializable

class CommonDialog : DialogFragment() {
    var contentsView: View? = null
    interface CommonDialogListener: Serializable {
        fun onPositive(view: View)
        fun onNegative(view: View) {}
        fun onCreated(view: View) {}
    }
    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_MESSAGE = "message"
        private const val ARG_LAYOUT_RESOURCE_ID = "resource_layout_id"
        private const val ARG_LISTENER = "listener"

        fun newInstance(title: String, message: String? = null, layout: Int? = null, listener: CommonDialogListener? = null): CommonDialog {
            val dialog = CommonDialog()
            val args = Bundle()
            args.putString(ARG_TITLE, title)
            if (message != null) args.putString(ARG_MESSAGE, message)
            if (layout != null) args.putInt(ARG_LAYOUT_RESOURCE_ID, layout)
            if (listener != null) args.putSerializable(ARG_LISTENER, listener)
            dialog.arguments = args
            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title = arguments?.getString(ARG_TITLE) ?: ""
        val message = arguments?.getString(ARG_MESSAGE)
        val listener = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable(ARG_LISTENER, CommonDialogListener::class.java)
        } else {
            arguments?.getSerializable(ARG_LISTENER) as CommonDialogListener
        }

        val builder = MaterialAlertDialogBuilder(requireContext(), R.style.Theme_CineBook_Dialog)

        val view = arguments?.getInt(ARG_LAYOUT_RESOURCE_ID)?.let {
            if (it == 0) null else layoutInflater.inflate(it, null)
        }

        contentsView = view

        builder.apply {
            setTitle(title)

            if (message != null) setMessage(message)
            if (view != null) setView(view)

            setPositiveButton(android.R.string.ok) { _, _ ->
                if (view != null) listener?.onPositive(view)
                dismiss()
            }
            setNegativeButton(android.R.string.cancel) { _, _ ->
                if (view != null) listener?.onNegative(view)
                dismiss()
            }
        }

        return builder.create().apply {
            // Dismiss when tapping outside the dialog
            setCanceledOnTouchOutside(true)
            view?.let { listener?.onCreated(it) }
        }
    }
}
