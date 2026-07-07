package com.example.moviceapp.book

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.example.moviceapp.R
import com.example.moviceapp.book.BookInfo.SEAT
import com.example.moviceapp.book.BookInfo.SHOWTIME
import com.example.moviceapp.book.BookInfo.THEATER
import com.example.moviceapp.book.choose.adapter.BookChooseInformationAdapter
import com.example.moviceapp.book.choose.adapter.MoviePagerAdapter
import com.example.moviceapp.databinding.FragmentBookChooseInfoBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BookChooseInfoFragment : Fragment(), BookChooseHandler {
    private var _binding: FragmentBookChooseInfoBinding? = null
    private val binding: FragmentBookChooseInfoBinding
        get() = _binding!!
    private val args: BookChooseInfoFragmentArgs by navArgs()
    @Inject
    lateinit var movieAssistedFactory: BookChooseInfoViewModel.MovieAssistedFactory
    private lateinit var movieAdapter: MoviePagerAdapter
    private lateinit var chooseInfoAdapter: BookChooseInformationAdapter
    private val viewModel: BookChooseInfoViewModel by viewModels {
        BookChooseInfoViewModel.provideFactory(
            movieAssistedFactory, args.selectedMovie)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookChooseInfoBinding.inflate(
            inflater, container, false)
        chooseInfoAdapter = BookChooseInformationAdapter(viewModel)
        movieAdapter = MoviePagerAdapter(args.movies.toList())
        viewModel.chooseHandler = this
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Movie ViewPager2
        binding.movieViewPager.adapter = movieAdapter
        binding.movieViewPager.setCurrentItem(args.movies.indexOf(args.selectedMovie), false)
        binding.movieViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewModel.actionMoviePageMoved(args.movies[position])
            }
        })
        // Booking ChooseInfo ViewPager2
        binding.movieChooseInfoViewPager.adapter = chooseInfoAdapter
        binding.movieChooseInfoViewPager.currentItem = 0
        binding.movieChooseInfoViewPager.isEnabled = false
        // Go Next Button
        binding.goNextButton.setOnClickListener { viewModel.actionGoNextButton() }
        // Subscribe viewModel
        lifecycleScope.launch {
            viewModel.model.collect { model ->
                val isEnabled = when (model.currentBookInfo) {
                    THEATER ->   model.selectedTheater != null
                    SHOWTIME -> (model.selectedShowtime?.selectedShowtimeSlot != null)
                    SEAT ->     (model.selectedSeat != null)
                }
                // Go Next Button Status
                binding.goNextButton.isEnabled = isEnabled
                // Go Next Button Background color
                val tint = if (isEnabled) R.color.green_accent else R.color.background_secondary
                binding.goNextButton.backgroundTintList = ContextCompat.getColorStateList(
                    requireContext(), tint)
                // Go Next Button Text color
                val textColor = if (isEnabled) R.color.badge_text else R.color.text_primary
                binding.goNextButton.setTextColor(ContextCompat.getColor(
                    requireContext(), textColor))
            }
        }
        lifecycleScope.launch {
            viewModel.theaterList.collect { chooseInfoAdapter.theaters = it }
        }
        lifecycleScope.launch {
            viewModel.showDateList.collect { chooseInfoAdapter.showDateList = it }
        }
        lifecycleScope.launch {
            viewModel.showTimeList.collect { chooseInfoAdapter.showTimeList = it }
        }
        lifecycleScope.launch {
            viewModel.seatList.collect { chooseInfoAdapter.seats = it }
        }
        // Action on viewCreated using ViewModel
        viewModel.actionOnViewCreated()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun goNextAnimated(
        targetPosition: BookInfo,
        duration: Long
    ) {
        val bottomPager = binding.movieChooseInfoViewPager
        if (bottomPager.currentItem.toBookInfo == targetPosition) return

        val current = bottomPager.currentItem
        val target = targetPosition.currentItem

        if (current == -1 || target == -1) return

        val root = bottomPager.parent as ViewGroup
        val direction = if (target < current) 1 else -1

        // 1. 현재 화면 스냅샷
        val snapshot = bottomPager.drawToBitmap() // androidx.core.view.drawToBitmap
        val overlay = ImageView(bottomPager.context).apply {
            setImageBitmap(snapshot)
            layoutParams = ViewGroup.LayoutParams(bottomPager.width, bottomPager.height)
            x = bottomPager.x
            y = bottomPager.y
        }
        root.addView(overlay)

        // 2. 애니메이션 없이 즉시 점프 (오버레이 뒤에서 일어나므로 안 보임)
        bottomPager.setCurrentItem(target, false)

        // 3. 다음 프레임에 실제 애니메이션 실행
        bottomPager.post {
            bottomPager.translationX = -direction * bottomPager.width.toFloat()
            bottomPager.animate()
                .translationX(0f)
                .setDuration(duration)
                .start()

            overlay.animate()
                .translationX(direction * bottomPager.width.toFloat())
                .setDuration(duration)
                .withEndAction { root.removeView(overlay) }
                .start()
        }
    }
}
enum class BookInfo { THEATER, SHOWTIME, SEAT }
val BookInfo.currentItem: Int
    get() = when (this) {
        THEATER -> 0
        SHOWTIME -> 1
        SEAT -> 2
    }
val Int.toBookInfo: BookInfo?
    get() = when (this) {
        0 -> THEATER
        1 -> SHOWTIME
        2 -> SEAT
        else -> null
    }
interface BookChooseHandler {
    fun goNextAnimated(
        targetPosition: BookInfo,
        duration: Long = 300
    )
}