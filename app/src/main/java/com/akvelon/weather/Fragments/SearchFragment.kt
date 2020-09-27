package com.akvelon.weather.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.akvelon.weather.MainActivity
import com.akvelon.weather.R
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import kotlinx.android.synthetic.main.location_hint.view.*


class SearchFragment: Fragment() {
    private val MAX_HINTS = 3
    private lateinit var constraintLayout: ConstraintLayout
    private lateinit var autoCompleteTextView: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private var hintList = mutableListOf<AutocompletePrediction>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_search, container, false)
        linearLayoutManager = LinearLayoutManager(requireContext())
        viewAdapter = CustomAdapter()
        recyclerView = view.findViewById<RecyclerView>(R.id.hintRecyclerView).apply {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
            adapter = viewAdapter
        }

        constraintLayout = view.findViewById(R.id.constrainLayout)
        constraintLayout.setOnClickListener {
            closeKeyboard()
            (activity as MainActivity).closeSearch()
        }

        autoCompleteTextView = view.findViewById(R.id.searchField)
        autoCompleteTextView.requestFocus()
        val searchButton = view.findViewById<ImageButton>(R.id.searchButton)
        searchButton.setOnClickListener {
            autoCompleteTextView.setText("")
        }

        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                if(autoCompleteTextView.text.toString() == "") {
                    searchButton.setImageDrawable(resources.getDrawable(R.drawable.baseline_search_black_24dp, null))
                } else {
                    searchButton.setImageDrawable(resources.getDrawable(R.drawable.baseline_clear_black_24dp, null))
                }
                getAutocompletePredictions(s)
            }
        })

        showKeyboard()
        return view;
    }

    inner class CustomAdapter() : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {
        inner class ViewHolder(view: FrameLayout) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.location_hint, parent, false) as FrameLayout
            return ViewHolder(view)
        }

        override fun getItemCount() = hintList.count()

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val card = holder.itemView as FrameLayout
            card.place.text = hintList[position].getFullText(null)
            if (position == hintList.count() - 1) {
                card.hints_divider.background = null
            }

            card.setOnClickListener {
                (activity as MainActivity).getCurrentPlace(hintList[position].placeId)
                constraintLayout.callOnClick()
            }
        }
    }

    private fun showKeyboard() {
        val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun closeKeyboard() {
        val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager?.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }

    private fun getAutocompletePredictions(s: CharSequence) {
        val token = AutocompleteSessionToken.newInstance()
        val request = FindAutocompletePredictionsRequest.builder()
            .setTypeFilter(TypeFilter.CITIES)
            .setSessionToken(token)
            .setQuery(s.toString())
            .build()

        Places.createClient(requireContext()).findAutocompletePredictions(request)
            .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                val autocompletePredictionCount = response.autocompletePredictions.size
                hintList = if(autocompletePredictionCount > MAX_HINTS) {
                    response.autocompletePredictions.subList(0, MAX_HINTS)
                } else {
                    response.autocompletePredictions.subList(0, autocompletePredictionCount)
                }

                viewAdapter.notifyDataSetChanged()
            }
    }
}