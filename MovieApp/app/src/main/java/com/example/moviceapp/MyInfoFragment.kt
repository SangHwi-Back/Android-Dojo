package com.example.moviceapp

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import android.graphics.Rect
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

        binding.userProfileNameTextView.text = "Guest User"
        binding.userProfileStatusTextView.text = "Sign in to access your bookings"

        // HISTORY
        val historyAdapter = HistoryListAdapter()
        val spacingPx = (8 * resources.displayMetrics.density).toInt()
        binding.myInfoHistoryRecyclerView.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        binding.myInfoHistoryRecyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                val position = parent.getChildAdapterPosition(view)
                outRect.left = if (position == 0) 0 else spacingPx
            }
        })
        binding.myInfoHistoryRecyclerView.adapter = historyAdapter
        historyAdapter.submitList(listOf(
            MyInfoHistory("12", "Movies"),
            MyInfoHistory("1.2K", "Points"),
            MyInfoHistory("$89", "Saved")
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
                "My Bookings"),
            MyInfoStatusSection(
                getDrawable(R.drawable.credit_card_24px)!!,
                "",
                "Payment Methods"),
            MyInfoStatusSection(
                getDrawable(R.drawable.notifications_24px)!!,
                "5",
                "Notifications"),
            MyInfoStatusSection(
                getDrawable(R.drawable.star_outlined_24px)!!,
                "",
                "Reward & Points",
                "1,250 points"),
            MyInfoStatusSection(
                getDrawable(R.drawable.settings_24px)!!,
                "",
                "Settings"),
            MyInfoStatusSection(
                getDrawable(R.drawable.help_outlined_24px)!!,
                "",
                "Help & Support"),
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
            val binding = ItemMyInfoHistoryBinding.inflate(inflater, parent, false)
            val spacingPx = (8 * parent.context.resources.displayMetrics.density).toInt()
            // 아이템 3개, 사이 gap 2개(8dp) → 각 아이템 너비 = (RecyclerView 너비 - 8dp × 2) / 3
            val itemWidth = (parent.measuredWidth - spacingPx * 2) / 3
            binding.root.layoutParams = binding.root.layoutParams.apply {
                width = itemWidth
            }
            return HistoryViewHolder(binding)
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
            binding.movieImageView.load(movie.posterURL ?: R.drawable.ic_launcher_background)
        }
    }

    class HistoryViewHolder(
        val binding: ItemMyInfoHistoryBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(history: MyInfoHistory) {
            binding.numberTextView.text = history.number
            binding.nameTextView.text = history.name
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
    val number: String,
    val name: String,
)
data class MyInfoStatusSection(
    val drawable: Drawable,
    val badge: String,
    val title: String,
    val subTitle: String? = null,
)