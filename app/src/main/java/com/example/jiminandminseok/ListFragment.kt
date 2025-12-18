package com.example.jiminandminseok

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListFragment : Fragment(R.layout.fragment_list) {

    private lateinit var adapter: RecordAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.recyclerView)

        adapter = RecordAdapter { item ->
            // createdAt을 키로 상세화면에서 다시 DB에서 찾게 함
            val args = bundleOf("createdAt" to item.createdAt)
            findNavController().navigate(R.id.detailFragment, args)
        }

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        loadRecords()
    }

    override fun onResume() {
        super.onResume()
        loadRecords()
    }

    private fun loadRecords() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val items = withContext(Dispatchers.IO) {
                    DbProvider.get(requireContext().applicationContext)
                        .recordDao()
                        .getAll()
                }

                Log.d("LIST", "items size = ${items.size}")
                adapter.submitList(items)

                if (items.isEmpty()) {
                    Toast.makeText(requireContext(), "저장된 기록이 없습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: CancellationException) {
                Log.d("LIST", "loadRecords cancelled")
            } catch (e: Exception) {
                Log.e("LIST", "loadRecords failed", e)
                Toast.makeText(requireContext(), "불러오기 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
