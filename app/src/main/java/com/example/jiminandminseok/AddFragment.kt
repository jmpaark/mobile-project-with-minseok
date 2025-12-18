package com.example.jiminandminseok

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddFragment : Fragment(R.layout.fragment_add) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etCount = view.findViewById<TextInputEditText>(R.id.etCount)
        val etMemo = view.findViewById<TextInputEditText>(R.id.etMemo)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        btnSave.setOnClickListener {
            val countStr = etCount.text?.toString()?.trim()
            val count = countStr?.toIntOrNull()

            if (count == null) {
                etCount.error = "숫자를 입력하세요"
                return@setOnClickListener
            }

            val memo = etMemo.text?.toString()?.trim()

            lifecycleScope.launch(Dispatchers.IO) {
                DbProvider.get(requireContext())
                    .recordDao()
                    .insert(RecordEntity(count = count, memo = memo))

                launch(Dispatchers.Main) {
                    // 저장 후 목록으로 이동(없으면 그냥 popBackStack 해도 됨)
                    findNavController().navigate(R.id.action_addFragment_to_listFragment)
                }
            }
        }
    }
}
