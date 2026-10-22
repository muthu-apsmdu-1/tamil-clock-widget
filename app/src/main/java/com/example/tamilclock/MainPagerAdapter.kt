package com.example.tamilclock

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MainPagerAdapter : RecyclerView.Adapter<MainPagerAdapter.TabViewHolder>() {

    private val tabTitles = listOf("பஞ்சாங்கம்", "ராசிபலன்", "ஹோரை")

    class TabViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            android.R.layout.simple_list_item_1,
            parent,
            false
        )
        return TabViewHolder(view)
    }

    override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
        holder.textView.text = "${tabTitles[position]} பக்கம் விரைவில்..."
        holder.textView.textSize = 18f
        holder.textView.setPadding(32, 64, 32, 32)
        holder.textView.setTextColor(0xFF2C1A14.toInt())
    }

    override fun getItemCount(): Int = tabTitles.size
}
