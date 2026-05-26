package com.example.moviceapp

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.moviceapp.databinding.FragmentMyInfoBinding
import com.example.moviceapp.databinding.ItemMyInfoHistoryBinding
import com.example.moviceapp.databinding.ItemMyInfoUpcomingMovieBinding
import com.example.moviceapp.databinding.ItemMyInfoUserStatusSectionBinding

class MyInfoFragment : Fragment() {
    private var _binding: FragmentMyInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // HISTORY
        val historyAdapter = HistoryListAdapter()
        binding.myInfoHistoryRecyclerView.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        binding.myInfoHistoryRecyclerView.adapter = historyAdapter
        historyAdapter.submitList(listOf(
            MyInfoHistory("Movies", "12"),
            MyInfoHistory("Points", "1.2K"),
            MyInfoHistory("Saved", "$89")
        ))

        // UPCOMING_MOVIE
        val upcomingAdapter = UpcomingMovieListAdapter()
        binding.myInfoUpcomingMovieRecyclerView.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        binding.myInfoUpcomingMovieRecyclerView.adapter = upcomingAdapter
        upcomingAdapter.submitList(MoviesMock.comingSoon)

        // USER_STATUS_SECTION
        fun getDrawable(id: Int) = ContextCompat.getDrawable(requireContext(), id)
        val statusAdapter = UserStatusSectionListAdapter()
        binding.myInfoUserStatusRecyclerView.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        binding.myInfoUserStatusRecyclerView.adapter = statusAdapter
        statusAdapter.submitList(listOf(
            MyInfoStatusSection(
                getDrawable(R.drawable.confirmation_number_outlined_24px)!!,
                "3",
                "My Bookings")
        ))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // --- History ---
    class HistoryListAdapter : ListAdapter<MyInfoHistory, HistoryViewHolder>(HistoryDiffCallback) {
        object HistoryDiffCallback : DiffUtil.ItemCallback<MyInfoHistory>() {
            override fun areItemsTheSame(oldItem: MyInfoHistory, newItem: MyInfoHistory): Boolean =
                oldItem.name == newItem.name
            override fun areContentsTheSame(oldItem: MyInfoHistory, newItem: MyInfoHistory): Boolean =
                oldItem == newItem
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return HistoryViewHolder(ItemMyInfoHistoryBinding.inflate(inflater, parent, false))
        }
        override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) =
            holder.bind(getItem(position))
    }

    // --- Upcoming Movie ---
    class UpcomingMovieListAdapter : ListAdapter<Movie, UpcomingMovieViewHolder>(MovieDiffCallback) {
        object MovieDiffCallback : DiffUtil.ItemCallback<Movie>() {
            override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean =
                oldItem.title == newItem.title
            override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean =
                oldItem == newItem
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UpcomingMovieViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return UpcomingMovieViewHolder(ItemMyInfoUpcomingMovieBinding.inflate(inflater, parent, false))
        }
        override fun onBindViewHolder(holder: UpcomingMovieViewHolder, position: Int) =
            holder.bind(getItem(position))
    }

    // --- User Status Section ---
    class UserStatusSectionListAdapter : ListAdapter<MyInfoStatusSection, UserStatusSectionViewHolder>(StatusDiffCallback) {
        object StatusDiffCallback : DiffUtil.ItemCallback<MyInfoStatusSection>() {
            override fun areItemsTheSame(oldItem: MyInfoStatusSection, newItem: MyInfoStatusSection): Boolean =
                oldItem.title == newItem.title
            override fun areContentsTheSame(oldItem: MyInfoStatusSection, newItem: MyInfoStatusSection): Boolean =
                oldItem.title == newItem.title && oldItem.badge == newItem.badge
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserStatusSectionViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return UserStatusSectionViewHolder(ItemMyInfoUserStatusSectionBinding.inflate(inflater, parent, false))
        }
        override fun onBindViewHolder(holder: UserStatusSectionViewHolder, position: Int) =
            holder.bind(getItem(position))
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