package com.akvelon.weather

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class WeekFragment : Fragment() {
    private val DAYS_NUMBER = 7

    private lateinit var recyclerView: RecyclerView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var viewAdapter: RecyclerView.Adapter<*>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_week, container, false)
        linearLayoutManager = LinearLayoutManager(requireContext())
        viewAdapter = CustomAdapter()
        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView).apply {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
            adapter = viewAdapter
        }

        return view
    }

    inner class CustomAdapter() : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {
        inner class ViewHolder(view: MaterialCardView) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.day_info, parent, false) as MaterialCardView
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = DAYS_NUMBER

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        }
    }
}