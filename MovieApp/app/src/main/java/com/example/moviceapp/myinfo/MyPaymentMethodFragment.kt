// app/src/main/java/com/example/moviceapp/myinfo/MyPaymentMethodFragment.kt
package com.example.moviceapp.myinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.moviceapp.AppViewModel
import com.example.moviceapp.databinding.FragmentMyPaymentMethodBinding
import com.example.moviceapp.databinding.ItemMyPaymentMethodAddButtonBinding
import com.example.moviceapp.databinding.ItemMyPaymentMethodBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyPaymentMethodFragment : Fragment() {
    private var _binding: FragmentMyPaymentMethodBinding? = null
    private val binding get() = _binding!!
    private val cardViewModel: CardViewModel by viewModels()
    private val appViewModel: AppViewModel by activityViewModels()
    private lateinit var adapter: MyPaymentAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyPaymentMethodBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.adapter = MyPaymentAdapter(navController = findNavController())
        binding.paymentRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.paymentRecyclerView.adapter = adapter

        // Observe cards from ViewModel using coroutine
        lifecycleScope.launch {
            cardViewModel.cards.collect { cards ->
                adapter.submitList(cards)
            }
        }

        // Observe loading state from AppViewModel
        lifecycleScope.launch {
            cardViewModel.isLoading.collect { isLoading ->
                if (isLoading)
                    appViewModel.showLoading()
                else
                    appViewModel.hideLoading()
            }
        }

        // Observe error from AppViewModel
        lifecycleScope.launch {
            cardViewModel.error.collect { error ->
                if (error != null)
                    appViewModel.handleException(error)
            }
        }

        // Load cards
        cardViewModel.loadCards()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class MyPaymentAdapter(val navController: NavController) : ListAdapter<com.example.moviceapp.repo.PaymentMethodDto, MyPaymentAdapter.ViewHolder>(DiffCallback) {

        companion object {
            private const val VIEW_TYPE_ITEM = 0
            private const val VIEW_TYPE_ADD = 1
        }

        override fun getItemCount() = super.getItemCount() + 1

        override fun getItemViewType(position: Int) =
            if (position == itemCount - 1) VIEW_TYPE_ADD else VIEW_TYPE_ITEM

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return if (viewType == VIEW_TYPE_ADD)
                ViewHolder(ItemMyPaymentMethodAddButtonBinding.inflate(inflater, parent, false)).apply {
                    binding.root.setOnClickListener {
                        val directions = MyPaymentMethodFragmentDirections.actionMyPaymentMethodToAddCardFragment()
                        navController.navigate(directions)
                    }
                }
            else
                ViewHolder(ItemMyPaymentMethodBinding.inflate(inflater, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (getItemViewType(position) == VIEW_TYPE_ADD) return
            holder.bind(getItem(position))
        }

        object DiffCallback : DiffUtil.ItemCallback<com.example.moviceapp.repo.PaymentMethodDto>() {
            override fun areItemsTheSame(oldItem: com.example.moviceapp.repo.PaymentMethodDto, newItem: com.example.moviceapp.repo.PaymentMethodDto) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: com.example.moviceapp.repo.PaymentMethodDto, newItem: com.example.moviceapp.repo.PaymentMethodDto) = oldItem == newItem
        }

        class ViewHolder(val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(paymentMethod: com.example.moviceapp.repo.PaymentMethodDto) {
                if (binding !is ItemMyPaymentMethodBinding) return

                binding.paymentMethodIdTextView.text = paymentMethod.maskedNumber
                binding.paymentMethodDescriptionTextView.text = paymentMethod.expiryDate
                binding.statusButton.visibility =
                    if (paymentMethod.isDefault) View.VISIBLE else View.INVISIBLE
            }
        }
    }
}
