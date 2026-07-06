package com.example.moviceapp.book.choose.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moviceapp.book.BookChooseInfoViewModel
import com.example.moviceapp.book.choose.viewholder.BookChooseViewHolder
import com.example.moviceapp.book.choose.viewholder.BookChooseViewHolderFactory

class BookChooseInformationAdapter(
    private val viewModel: BookChooseInfoViewModel
) : RecyclerView.Adapter<BookChooseViewHolder>() {
    override fun getItemCount(): Int = 3
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BookChooseViewHolder {
        return BookChooseViewHolderFactory.createViewHolder(parent, viewType)
    }
    override fun onBindViewHolder(
        holder: BookChooseViewHolder,
        position: Int
    ) {
        holder.bind(viewModel.model.value)
    }
}