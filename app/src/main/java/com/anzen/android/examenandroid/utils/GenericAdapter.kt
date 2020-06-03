package com.anzen.android.examenandroid.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Ignore
import com.anzen.android.examenandroid.BR
import com.anzen.android.examenandroid.R
import java.util.*

/**
 * Created by Diego Martinez on 2020-07-01
 */

class GenericAdapter<T : ListItemViewModel>(@LayoutRes val layoutId: Int, val percentageWith: Float = 0f, val percentageHeight: Float = 0f, val isAll: Boolean = false) :
    RecyclerView.Adapter<GenericAdapter.GenericViewHolder<T>>() {

    private val items = mutableListOf<T>()
    private var inflater: LayoutInflater? = null
    private var onListItemViewClickListener: OnListItemViewClickListener? = null

    fun addItems(items: List<T>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    fun getItem(position: Int): T? {
        if(position >= this.items.size) return null
        return this.items[position]
    }

    fun getItems() = items

    fun removeItem(position: Int) {
        this.items.removeAt(position)
        this.notifyItemRemoved(position)
        notifyItemRangeChanged(position,items.size)
    }

    fun restoreItem(item: T, position: Int) {
        this.items.add(position, item)
        notifyItemInserted(position)
    }

    fun setOnListItemViewClickListener(onListItemViewClickListener: OnListItemViewClickListener?){
        this.onListItemViewClickListener = onListItemViewClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericViewHolder<T> {
        val layoutInflater = inflater ?: LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(layoutInflater, layoutId, parent, false)
        return GenericViewHolder(
            binding,
            percentageHeight,
            percentageWith,
            isAll
        )
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: GenericViewHolder<T>, position: Int) {
        val itemViewModel = items[position]
        itemViewModel.adapterPosition = position
        onListItemViewClickListener?.let { itemViewModel.onListItemViewClickListener = it }
        holder.bind(itemViewModel)
    }

    class GenericViewHolder<T : ListItemViewModel>(private val binding: ViewDataBinding, percentageHeight: Float = 0f, percentageWith: Float = 0f, isAll: Boolean = false) :
        RecyclerView.ViewHolder(binding.root) {

        private val mRandom = Random()
        val ext = itemView.resources.getInteger(R.integer.staggeredGridLayoutExt)
        val min = itemView.resources.getInteger(R.integer.staggeredGridLayoutMin)
        val max = itemView.resources.getInteger(R.integer.staggeredGridLayoutMax)


        fun bind(itemViewModel: T) {
            binding.setVariable(BR.listItemViewModel, itemViewModel)
            binding.executePendingBindings()
        }

        private fun getRandomIntInRange(max: Int, min: Int): Int {
            return mRandom.nextInt(max - min + min) + min
        }

    }

    interface OnListItemViewClickListener{
        fun onClick(view: View, position: Int)
    }
}

abstract class ListItemViewModel{
    @Ignore
    var adapterPosition: Int = -1
    @Ignore
    var onListItemViewClickListener: GenericAdapter.OnListItemViewClickListener? = null
}