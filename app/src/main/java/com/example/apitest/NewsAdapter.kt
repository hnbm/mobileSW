package com.example.apitest

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NewsAdapter(myDataset: List<NewsData>?, context: Context?, onClick: View.OnClickListener) : RecyclerView.Adapter<NewsAdapter.MyViewHolder>() {
    private var mDataset: List<NewsData>? = null
    private var onClickListener: View.OnClickListener? = null

    init {
        mDataset = myDataset
        onClickListener = onClick
    }

    inner class MyViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var textViewTitle: TextView
        var rootView: View

        init {
            textViewTitle = v.findViewById(R.id.TextViewTitle)
            rootView = v
            v.setClickable(true)
            v.setEnabled(true)
            v.setOnClickListener(onClickListener)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // create a new view
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.snews_item01, parent, false) as LinearLayout
        return MyViewHolder(v)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val news = mDataset!![position]
        holder.textViewTitle.text = news.getTitle()
        holder.rootView.setTag(position)
    }

    override fun getItemCount(): Int {
        return if (mDataset == null) 0 else mDataset!!.size
    }

    fun getNews(position: Int): NewsData? {
        return if (mDataset != null) mDataset!![position] else null
    }
}