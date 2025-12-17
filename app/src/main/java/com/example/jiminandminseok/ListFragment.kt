package com.example.jiminandminseok

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ListFragment : Fragment(R.layout.fragment_list) {

    private lateinit var adapter: RecordAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.recyclerView)
        adapter = RecordAdapter()

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        loadRecords()
    }

    override fun onResume() {
        super.onResume()
        loadRecords() // Add에서 저장하고 돌아오면 갱신
    }

    private fun loadRecords() {
        lifecycleScope.launch(Dispatchers.IO) {
            val items = DbProvider.get(requireContext()).recordDao().getAll()
            launch(Dispatchers.Main) {
                adapter.submitList(items)
            }
        }
    }
}
