package uz.gita.myphotoeditor_bek.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import uz.gita.myphotoeditor_bek.databinding.ItemColorBinding

class TextColorAdapter : Adapter<TextColorAdapter.ItemHolder>() {

    private var list = ArrayList<Int>()

    fun setData(l: ArrayList<Int>) {
        list = l
        notifyDataSetChanged()
    }

    private var touchListener: ((Int) -> Unit)? = null

    fun setTouchListener(block: (Int) -> Unit) {
        touchListener = block
    }

    inner class ItemHolder(private val binding: ItemColorBinding) :
        ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                touchListener?.invoke(list[adapterPosition])
            }
        }

        fun bind() {
            binding.viewColor.background =
                ContextCompat.getDrawable(binding.root.context, list[adapterPosition])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(
            ItemColorBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.bind()
    }
}