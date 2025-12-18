package com.example.jiminandminseok

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailFragment : Fragment(R.layout.fragment_detail) {

    private val dfDate = SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA)
    private var createdAt: Long = -1L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            if (!findNavController().navigateUp()) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }


        val tvDate = view.findViewById<TextView>(R.id.tvDetailDate)
        val tvCount = view.findViewById<TextView>(R.id.tvDetailCount)
        val tvMemo = view.findViewById<TextView>(R.id.tvDetailMemo)
        val btnEdit = view.findViewById<Button>(R.id.btnEdit)
        val btnDelete = view.findViewById<Button>(R.id.btnDelete)

        createdAt = arguments?.getLong("createdAt", -1L) ?: -1L
        if (createdAt <= 0L) {
            Toast.makeText(requireContext(), "잘못된 접근입니다.", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        fun render(item: RecordEntity) {
            tvDate.text = dfDate.format(Date(item.createdAt))
            tvCount.text = "${item.count}개"
            tvMemo.text = item.memo?.takeIf { it.isNotBlank() } ?: "메모 없음"
        }

        // ✅ 값 불러와서 표시
        viewLifecycleOwner.lifecycleScope.launch {
            val item = withContext(Dispatchers.IO) {
                DbProvider.get(requireContext().applicationContext)
                    .recordDao()
                    .getByCreatedAt(createdAt)
            }

            if (item == null) {
                Toast.makeText(requireContext(), "기록을 찾지 못했습니다.", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
                return@launch
            }

            render(item)

            // ✅ 수정하기: AddFragment로 값 들고 이동(너희 AddFragment가 args 받으면 편집 가능)
            btnEdit.setOnClickListener {
                val args = bundleOf(
                    "mode" to "edit",
                    "createdAt" to item.createdAt,
                    "count" to item.count,
                    "memo" to (item.memo ?: "")
                )
                findNavController().navigate(R.id.addFragment, args)
            }

            // ✅ 삭제하기: createdAt 기준으로 삭제
            btnDelete.setOnClickListener {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("삭제할까?")
                    .setMessage("이 기록을 삭제하면 되돌릴 수 없어.")
                    .setPositiveButton("삭제") { _, _ ->
                        viewLifecycleOwner.lifecycleScope.launch {
                            val rows = withContext(Dispatchers.IO) {
                                DbProvider.get(requireContext().applicationContext)
                                    .recordDao()
                                    .deleteByCreatedAt(item.createdAt)
                            }
                            if (rows > 0) {
                                Toast.makeText(requireContext(), "삭제 완료", Toast.LENGTH_SHORT).show()
                                findNavController().popBackStack()
                            } else {
                                Toast.makeText(requireContext(), "삭제 실패(기록 없음)", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .setNegativeButton("취소", null)
                    .show()
            }
        }
    }
}
