package com.sameh.pdfreader.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sameh.pdfreader.databinding.ItemPdfBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class PDFsAdapter : ListAdapter<File, PDFsAdapter.ViewHolder>(PDFDiffCallBack()) {

    private var pdfClickListener: ((File) -> Unit)? = null

    fun onPDFClickListener(lambdaEX: ((File) -> Unit)) {
        pdfClickListener = lambdaEX
    }

    fun setData(pdfList: ArrayList<File>) {
        this.submitList(pdfList)
    }

    inner class ViewHolder(private val binding: ItemPdfBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SimpleDateFormat", "SetTextI18n")
        fun bind(pdf: File) {
            binding.apply {
                tvPdfTitle.text = pdf.name
                tvPdfFolder.text = pdf.parentFile?.name

                val lastModifiedMillis = pdf.lastModified()
                val lastModifiedDate =
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(lastModifiedMillis))

                tvPdfDate.text = lastModifiedDate
                tvPdfSize.text = "${(pdf.length() / 1024)} KB"

                root.setOnClickListener {
                    pdfClickListener?.invoke(pdf)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPdfBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }
}

class PDFDiffCallBack : DiffUtil.ItemCallback<File>() {

    override fun areItemsTheSame(oldItem: File, newItem: File): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: File, newItem: File): Boolean {
        return oldItem == newItem
    }
}