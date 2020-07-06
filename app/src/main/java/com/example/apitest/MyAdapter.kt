package com.example.apitest

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlin.coroutines.coroutineContext

class MyAdapter(private val jangoList: ArrayList<Jango>) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    var clickedJongmokCode: String = ""
    private lateinit var itemClickListener: ItemClickListener

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var jongmokCode: String = ""
        var jongmokText = itemView.findViewById<TextView>(R.id.jongmok)
        var maeipgaText = itemView.findViewById<TextView>(R.id.maeipga)
        var sonikText = itemView.findViewById<TextView>(R.id.sonik)
        var maeip_amountText = itemView.findViewById<TextView>(R.id.maeip_amount)
        var boyou_amountText = itemView.findViewById<TextView>(R.id.boyou_amount)
        var suik_ryulText = itemView.findViewById<TextView>(R.id.suik_ryul)
        var hyunjaegaText = itemView.findViewById<TextView>(R.id.hyunjaega)
        var pyeongga_amounText = itemView.findViewById<TextView>(R.id.pyeongga_amount)
//        var rootView = itemView

        fun bind(jango: Jango) {
            jongmokCode = jango.jongmok_code
            jongmokText.text = jango.jongmok
            maeipgaText.text = jango.maeipga
            sonikText.text = jango.sonik
            maeip_amountText.text = jango.maeip_amount
            boyou_amountText.text = jango.boyou_amount
            suik_ryulText.text = jango.suik_ryul
            hyunjaegaText.text = jango.hyunjaega
            pyeongga_amounText.text = jango.pyeongga_amount

//            itemView.setOnClickListener { itemClick(jango) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyAdapter.MyViewHolder {
        val View = LayoutInflater.from(parent.context).inflate(R.layout.s1010_item01, parent, false)
//        View.setClickable(true)
//        View.setEnabled(true)
//        View.setOnClickListener(onClickListener)
        return MyViewHolder(View)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.jongmokText.setSingleLine(true)
        holder.jongmokText.setEllipsize(TextUtils.TruncateAt.MARQUEE)
        holder.jongmokText.setSelected(true)
        holder.bind(jangoList[position])
        Log.d("잔고하나하나", jangoList[position].jongmok)
        holder.itemView.setOnClickListener {
            Log.d("클릭!", position.toString())
            clickedJongmokCode = jangoList[position].jongmok_code
            itemClickListener.onClick(it, position)
        }
//        holder.rootView.setTag(position)
//        val jango = jangoList[position]
//        holder.apply {
//            bind(jango)
//        }
    }

    override fun getItemCount() = jangoList.size

    fun getJongmokCode() = clickedJongmokCode

    interface ItemClickListener {
        fun onClick(view: View, position: Int)
    }

    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }
}