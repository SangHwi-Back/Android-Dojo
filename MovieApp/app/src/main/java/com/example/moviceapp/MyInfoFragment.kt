package com.example.moviceapp

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.moviceapp.databinding.FragmentMyInfoBinding
import com.example.moviceapp.databinding.ItemMyInfoHistoryBinding
import com.example.moviceapp.databinding.ItemMyInfoUpcomingMovieBinding
import com.example.moviceapp.databinding.ItemMyInfoUserStatusSectionBinding

class MyInfoFragment : Fragment() {
    lateinit var binding: FragmentMyInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentMyInfoBinding.inflate(inflater)
        // HISTORY
        binding.myInfoHistoryRecyclerView.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        binding.myInfoHistoryRecyclerView.adapter =
            RecyclerViewAdapter(ViewHolderType.USER_STATUS_SECTION).also {
                it.setHistoryItems(listOf(
                    MyInfoHistory("Movies", "12"),
                    MyInfoHistory("Points", "1.2K"),
                    MyInfoHistory("Saved", "$89")
                ))
            }
        // UPCOMING_MOVIE
        binding.myInfoUpcomingMovieRecyclerView.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        binding.myInfoUpcomingMovieRecyclerView.adapter =
            RecyclerViewAdapter(ViewHolderType.UPCOMING_MOVIE).also {
                it.setMovieItems(MoviesMock.comingSoon) }
        // USER_STATUS_SECTION
        fun getDrawable(id: Int) =
            ContextCompat.getDrawable(requireContext(), id)
        binding.myInfoUserStatusRecyclerView.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        binding.myInfoUserStatusRecyclerView.adapter =
            RecyclerViewAdapter(ViewHolderType.USER_STATUS_SECTION).also {
                it.setStatusItems(listOf(
                    MyInfoStatusSection(
                        getDrawable(R.drawable.confirmation_number_outlined_24px)!!,
                        "3",
                        "My Bookings")
                ))
            }
        return binding.root
    }
    class RecyclerViewAdapter(
        private val adapterType: ViewHolderType
    ): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private var historyItems = mutableListOf<MyInfoHistory>()
        private var movieItems = mutableListOf<Movie>()
        private var statusItems = mutableListOf<MyInfoStatusSection>()
        override fun getItemViewType(position: Int): Int = adapterType.code
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            parent.context
            return when (viewType) {
                ViewHolderType.UPCOMING_MOVIE.code ->
                    UpcomingMovieViewHolder(ItemMyInfoUpcomingMovieBinding
                        .inflate(inflater, parent, false))
                ViewHolderType.HISTORY.code ->
                    HistoryViewHolder(ItemMyInfoHistoryBinding
                        .inflate(inflater, parent, false))
                else ->
                    UserStatusSectionViewHolder(ItemMyInfoUserStatusSectionBinding
                        .inflate(inflater, parent, false))
            }
        }
        override fun onBindViewHolder(
            holder: RecyclerView.ViewHolder,
            position: Int
        ) {
            when (holder) {
                is UpcomingMovieViewHolder -> holder.bind(movieItems[position])
                is HistoryViewHolder -> holder.bind(historyItems[position])
                is UserStatusSectionViewHolder -> holder.bind(statusItems[position])
            }
        }
        override fun getItemCount(): Int = when (adapterType) {
            ViewHolderType.UPCOMING_MOVIE -> movieItems.size
            ViewHolderType.USER_STATUS_SECTION -> statusItems.size
            ViewHolderType.HISTORY -> historyItems.size
        }
        fun setHistoryItems(items: List<MyInfoHistory>) {
            if (adapterType != ViewHolderType.HISTORY) return
            setItems(statusItems, items) {
                historyItems = items.toMutableList()
            }
        }
        fun setMovieItems(items: List<Movie>) {
            if (adapterType != ViewHolderType.UPCOMING_MOVIE) return
            setItems(statusItems, items) {
                movieItems = items.toMutableList()
            }
        }
        fun setStatusItems(items: List<MyInfoStatusSection>) {
            if (adapterType != ViewHolderType.USER_STATUS_SECTION) return
            setItems(statusItems, items) {
                statusItems = items.toMutableList()
            }
        }
        private fun setItems(
            prev: List<Any>,
            items: List<Any>,
            setItems: ()->Unit,
        ) {
            if (prev.size < items.size && prev.isNotEmpty())
                notifyItemRangeRemoved(prev.size-1, items.size-1)
            setItems()
            notifyItemRangeChanged(0, prev.size-1)
        }
    }
    class UpcomingMovieViewHolder(
        val binding: ItemMyInfoUpcomingMovieBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(movie: Movie) {
            binding.nameTextView.text = movie.title
            binding.pointTextView.text = movie.rating.toString()
            binding.movieImageView.load(movie.posterRes ?: R.drawable.ic_launcher_background)
        }
    }
    class HistoryViewHolder(
        val binding: ItemMyInfoHistoryBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(history: MyInfoHistory) {
            binding.nameTextView.text = history.name
            binding.contentsTextView.text = history.contents
        }
    }
    class UserStatusSectionViewHolder(
        val binding: ItemMyInfoUserStatusSectionBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(section: MyInfoStatusSection) {
            binding.iconSrc = section.drawable
            binding.badgeText = section.badge
            binding.sectionTitle = section.title
            binding.executePendingBindings()
        }
    }

    enum class ViewHolderType(val code: Int) {
        UPCOMING_MOVIE(0), HISTORY(1), USER_STATUS_SECTION(2)
    }
}

data class MyInfoHistory(
    val name: String,
    val contents: String,
)
data class MyInfoStatusSection(
    val drawable: Drawable,
    val badge: String,
    val title: String,
)