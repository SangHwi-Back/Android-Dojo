package com.example.moviceapp.myinfo

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.moviceapp.databinding.FragmentMyPaymentMethodBinding
import com.example.moviceapp.databinding.ItemMyPaymentMethodAddButtonBinding
import com.example.moviceapp.databinding.ItemMyPaymentMethodBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import java.util.Date
import kotlin.getValue

@AndroidEntryPoint
class MyPaymentMethodFragment : Fragment() {
    private var _binding: FragmentMyPaymentMethodBinding? = null
    private val binding get() = _binding!!
    private val cardViewModel: CardViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentMyPaymentMethodBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = MyPaymentAdapter(navController = findNavController())
        binding.paymentRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.paymentRecyclerView.adapter = adapter

        adapter.submitList(testPaymentMethods())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun testPaymentMethods() = listOf(
        PaymentMethod(
            id = 1,
            userUid = "test_uid",
            type = PaymentMethodType.CREDIT_CARD,
            billingKey = "bk_shin_4242",
            maskedNumber = "**** **** **** 4242",
            cardCompany = "신한",
            expiryDate = "Expires 08/28",
            cardholderName = "홍길동",
            pgCustomerUid = null,
            isDefault = true,
            isActive = true,
            nickname = "신한카드",
            createdAt = Date(),
        ),
        PaymentMethod(
            id = 2,
            userUid = "test_uid",
            type = PaymentMethodType.KAKAO_PAY,
            billingKey = null,
            maskedNumber = null,
            cardCompany = null,
            expiryDate = null,
            cardholderName = null,
            pgCustomerUid = "kakao_cuid_abc123",
            isDefault = false,
            isActive = true,
            nickname = "카카오페이",
            createdAt = Date(),
        ),
        PaymentMethod(
            id = 3,
            userUid = "test_uid",
            type = PaymentMethodType.DEBIT_CARD,
            billingKey = "bk_kb_1234",
            maskedNumber = "**** **** **** 1234",
            cardCompany = "국민",
            expiryDate = "Expires 12/26",
            cardholderName = "홍길동",
            pgCustomerUid = null,
            isDefault = false,
            isActive = true,
            nickname = "국민체크카드",
            createdAt = Date(),
        ),
    )

    class MyPaymentAdapter(val navController: NavController) : ListAdapter<PaymentMethod, MyPaymentAdapter.ViewHolder>(DiffCallback) {

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

        object DiffCallback : DiffUtil.ItemCallback<PaymentMethod>() {
            override fun areItemsTheSame(oldItem: PaymentMethod, newItem: PaymentMethod) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: PaymentMethod, newItem: PaymentMethod) = oldItem == newItem
        }

        class ViewHolder(val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(paymentMethod: PaymentMethod) {
                if (binding !is ItemMyPaymentMethodBinding) return

                binding.paymentMethodIdTextView.text =
                    paymentMethod.maskedNumber ?: paymentMethod.nickname ?: paymentMethod.type.name

                binding.paymentMethodDescriptionTextView.text =
                    paymentMethod.expiryDate ?: paymentMethod.type.label

                binding.statusButton.visibility =
                    if (paymentMethod.isDefault) View.VISIBLE else View.INVISIBLE
            }
        }
    }
}

enum class PaymentMethodType(val label: String) {
    CREDIT_CARD("신용카드"),
    DEBIT_CARD("체크카드"),
    KAKAO_PAY("카카오페이"),
    NAVER_PAY("네이버페이"),
    TOSS("토스"),
}

@Parcelize
data class PaymentMethod(
    val id: Int,
    val userUid: String,
    val type: PaymentMethodType,
    val billingKey: String?,
    val maskedNumber: String?,
    val cardCompany: String?,
    val expiryDate: String?,
    val cardholderName: String?,
    val pgCustomerUid: String?,
    val isDefault: Boolean,
    val isActive: Boolean,
    val nickname: String?,
    val createdAt: Date,
) : Parcelable
