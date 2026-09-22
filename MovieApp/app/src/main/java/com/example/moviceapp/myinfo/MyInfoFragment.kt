package com.example.moviceapp.myinfo

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.example.moviceapp.AppViewModel
import com.example.moviceapp.BuildConfig
import com.example.moviceapp.R
import com.example.moviceapp.common.CommonDialog
import com.example.moviceapp.databinding.FragmentMyInfoBinding
import com.example.moviceapp.databinding.ItemMyInfoHistoryBinding
import com.example.moviceapp.databinding.ItemMyInfoUpcomingMovieBinding
import com.example.moviceapp.databinding.ItemMyInfoUserStatusSectionBinding
import com.example.moviceapp.repo.Movie
import com.example.moviceapp.repo.MoviesMock
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyInfoFragment : Fragment() {
    private val accountSettingViewModel: AccountSettingViewModel by viewModels()
    private val appViewModel: AppViewModel by activityViewModels()
    private val myInfoViewModel: MyInfoViewModel by viewModels()
    private var _binding: FragmentMyInfoBinding? = null
    private val binding get() = _binding!!
    private lateinit var sectionAdapter: UserStatusSectionListAdapter
    private lateinit var historyAdapter: HistoryListAdapter
    private val sections = mutableListOf(
        MyInfoStatusSection(MyInfoSections.BOOKING, ""),
        MyInfoStatusSection(MyInfoSections.PAYMENT_METHOD, ""),
    )
    private val historyItems = mutableListOf(
        MyInfoHistory("12", "Movies"),
        MyInfoHistory("Developing", "in progress"),
    )
    private val isLoggedIn
        get() = accountSettingViewModel.currentUser.value != null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun showCommonDialog(title: String, message: String? = null) {
        CommonDialog.newInstance(title, message).show(
            childFragmentManager,
            "CommonDialog"
        )
    }

    override fun onStart() {
        super.onStart()
        accountSettingViewModel.getUserMe()
        myInfoViewModel.fetchBookings()
        lifecycleScope.launch {
            accountSettingViewModel.currentUser.collect { currentUser ->
                binding.userSignInButton.visibility = if (currentUser == null)
                    View.VISIBLE else View.GONE
                binding.userProfileNameTextView.text = currentUser?.name ?: getString(R.string.label_guest_user)
                binding.userProfileStatusTextView.visibility = if (currentUser == null)
                    View.VISIBLE else View.GONE
                setProfileImage(currentUser?.profileImageUrl)
            }
        }
        lifecycleScope.launch {
            sections.first { it.sectionType == MyInfoSections.BOOKING }
                .badge = myInfoViewModel.myBookings.value.size.toString()
            sectionAdapter.submitList(sections)
            historyItems.first { it.name == "Movies" }
                .number = myInfoViewModel.myBookings.value.size.toString()
            historyAdapter.submitList(historyItems)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appViewModel.setVisibilityFloatingActionButton(View.GONE)

        val user = Firebase.auth.currentUser
        binding.userProfileImageView.setOnClickListener {
            findNavController().navigate(if (isLoggedIn)
                MyInfoFragmentDirections.actionMyInfoFragmentToAccountSettingFragment()
            else
                MyInfoFragmentDirections.actionMyInfoFragmentToSignInFragment()
            )
        }
        binding.userProfileNameTextView.text = user?.displayName
            ?: getString(R.string.label_guest_user)
        binding.userProfileStatusTextView.text = user?.providerData?.firstOrNull()?.providerId
            ?: getString(R.string.label_guest_subtitle)
        binding.userSignInButton.setOnClickListener {
            requireActivity().setupAppBar(false)
            val directions = MyInfoFragmentDirections.actionMyInfoFragmentToSignInFragment()
            findNavController().navigate(directions)
        }

        // HISTORY
        historyAdapter = HistoryListAdapter()
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
        historyAdapter.submitList(historyItems)

        // UPCOMING_MOVIE
        val upcomingAdapter = UpcomingMovieListAdapter { movie ->
            showCommonDialog("This is ${movie.title}", movie.description)
        }
        binding.myInfoUpcomingMovieRecyclerView.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        binding.myInfoUpcomingMovieRecyclerView.adapter = upcomingAdapter
        upcomingAdapter.submitList(MoviesMock.comingSoon)

        // USER_STATUS_SECTION
        sectionAdapter = UserStatusSectionListAdapter {
            findNavController().navigate(it.sectionType.direction)
        }
        binding.myInfoUserStatusRecyclerView.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        binding.myInfoUserStatusRecyclerView.adapter = sectionAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        appViewModel.setVisibilityFloatingActionButton(View.VISIBLE)
        _binding = null
    }

    private suspend fun setProfileImage(profileImageUrl: String?) {
        val token = getAuthToken()
        if (profileImageUrl != null && token != null) {
            val fullUrl = "https://${BuildConfig.IP_API_SERVER}$profileImageUrl"
            binding.userProfileImageView.load(fullUrl) {
                addHeader("Authorization", token)
                addHeader("ngrok-skip-browser-warning", "true")
                transformations(CircleCropTransformation())
                placeholder(R.drawable.person_outlined_24px)
                error(R.drawable.person_outlined_24px)
            }
        } else {
            binding.userProfileImageView.setImageResource(R.drawable.person_outlined_24px)
        }
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
            // 아이템 2개, 사이 gap 2개(8dp) → 각 아이템 너비 = (RecyclerView 너비 - 8dp × 2) / 2
            val itemWidth = (parent.measuredWidth - spacingPx * 2) / 2
            binding.root.layoutParams = binding.root.layoutParams.apply {
                width = itemWidth
            }
            return HistoryViewHolder(binding)
        }
        override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) =
            holder.bind(getItem(position))
    }

    // --- Upcoming Movie ---
    class UpcomingMovieListAdapter(val onClick: (Movie) -> Unit) : ListAdapter<Movie, UpcomingMovieViewHolder>(MovieDiffCallback) {
        object MovieDiffCallback : DiffUtil.ItemCallback<Movie>() {
            override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean =
                oldItem.title == newItem.title
            override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean =
                oldItem == newItem
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UpcomingMovieViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return UpcomingMovieViewHolder(ItemMyInfoUpcomingMovieBinding.inflate(inflater, parent, false)).apply {
                binding.root.setOnClickListener {
                    onClick.invoke(getItem(bindingAdapterPosition))
                }
            }
        }
        override fun onBindViewHolder(holder: UpcomingMovieViewHolder, position: Int) =
            holder.bind(getItem(position))
    }

    // --- User Status Section ---
    class UserStatusSectionListAdapter(val onClick: (MyInfoStatusSection) -> Unit) : ListAdapter<MyInfoStatusSection, UserStatusSectionViewHolder>(StatusDiffCallback) {
        object StatusDiffCallback : DiffUtil.ItemCallback<MyInfoStatusSection>() {
            override fun areItemsTheSame(oldItem: MyInfoStatusSection, newItem: MyInfoStatusSection): Boolean =
                oldItem.sectionType == newItem.sectionType
            override fun areContentsTheSame(oldItem: MyInfoStatusSection, newItem: MyInfoStatusSection): Boolean =
                oldItem.sectionType == newItem.sectionType && oldItem.badge == newItem.badge
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserStatusSectionViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return UserStatusSectionViewHolder(ItemMyInfoUserStatusSectionBinding.inflate(inflater, parent, false)).apply {
                binding.root.setOnClickListener {
                    onClick.invoke(getItem(bindingAdapterPosition))
                }
            }
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
            binding.movieImageView.load(movie.posterUrl ?: R.drawable.ic_launcher_background)
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
            val type = section.sectionType
            binding.myInfoIcon.load(type.iconId)
            binding.badgeText = section.badge
            binding.sectionTitle = type.sectionName
            binding.executePendingBindings()
        }
    }
}

data class MyInfoHistory(
    var number: String,
    val name: String,
)
data class MyInfoStatusSection(
    val sectionType: MyInfoSections,
    var badge: String,
)
enum class MyInfoSections {
    BOOKING, PAYMENT_METHOD
}
val MyInfoSections.direction: NavDirections get() = when (this) {
    MyInfoSections.BOOKING -> MyInfoFragmentDirections.actionMyInfoFragmentToMyBookingsFragment()
    MyInfoSections.PAYMENT_METHOD -> MyInfoFragmentDirections.actionMyInfoFragmentToMyPaymentMethod()
}
val MyInfoSections.sectionName: String get() = when (this) {
    MyInfoSections.BOOKING -> "My Bookings"
    MyInfoSections.PAYMENT_METHOD -> "Payment Methods"
}
val MyInfoSections.iconId: Int get() = when (this) {
    MyInfoSections.BOOKING -> R.drawable.confirmation_number_outlined_24px
    MyInfoSections.PAYMENT_METHOD -> R.drawable.credit_card_24px
}
fun FragmentActivity.setupAppBar(isVisible: Boolean) {
    // This assumes you're using a toolbar with AppBarLayout
    // You'll need to adjust this based on your actual layout structure
    findViewById<AppBarLayout>(
        R.id.app_bar_layout // You'll need to define this in your activity layout
    ).visibility = if (isVisible) View.VISIBLE else View.GONE

    // Make navigation icon visible (back button)
    actionBar?.setDisplayHomeAsUpEnabled(isVisible)
    actionBar?.setDisplayShowHomeEnabled(isVisible)
}