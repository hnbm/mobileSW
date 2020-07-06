package com.example.apitest

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class news : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var queue: RequestQueue
    private lateinit var mAdapter: RecyclerView.Adapter<NewsAdapter.MyViewHolder>
    private lateinit var dataText: TextView
    lateinit var root: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.activity_news, container, false)
        recyclerView = root.findViewById(R.id.my_recycler_view)
        recyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(activity)
        recyclerView.setLayoutManager(layoutManager)

        // 1. 화면이 로딩 -> 뉴스 정보를 받아온다.
        queue = Volley.newRequestQueue(activity)
        getNews()

        // 2. 정보 -> 어댑터에 넘겨준다.
        // 3. 어댑터 -> 셋팅
        return root
    }

    fun getNews() {
        val url =
            "http://newsapi.org/v2/top-headlines?country=kr&category=business&apiKey=d40c3f302f6145efa7f7f5ca161dcf06"

        // Request a string response from the provided URL.
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener { response ->
                // Display the first 500 characters of the response string.
                //Log.d("NEWS", response);
                try {
                    val jsonObj = JSONObject(response)
                    val arrayArticles = jsonObj.getJSONArray("articles")

                    // response ->> NewsData.java Class 분류
                    val news: MutableList<NewsData> = ArrayList<NewsData>()
                    var i = 0
                    val j = arrayArticles.length()
                    while (i < j) {
                        val obj = arrayArticles.getJSONObject(i)
                        Log.d("NEWS", obj.toString())
                        val newsData = NewsData()
                        newsData.setTitle(obj.getString("title"))
                        newsData.setUrlToImage(obj.getString("url"))
//                        newsData.setContent(obj.getString("content"))
                        news.add(newsData)
                        i++
                    }

                    // specify an adapter (see also next example)
                    mAdapter =
                        NewsAdapter(news, activity, View.OnClickListener { v ->
                            if (v.tag != null) {
                                val position = v.tag as Int
                                var url = (mAdapter as NewsAdapter).getNews(position)!!.getUrlToImage()
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                startActivity(intent)
                            }
                        })
                    recyclerView.adapter = mAdapter
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { })

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}