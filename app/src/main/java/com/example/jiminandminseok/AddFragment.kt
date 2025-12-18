package com.example.jiminandminseok

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class AddFragment : Fragment(R.layout.fragment_add) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            if (!findNavController().navigateUp()) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        val etCount = view.findViewById<TextInputEditText>(R.id.etCount)
        val etMemo = view.findViewById<TextInputEditText>(R.id.etMemo)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        btnSave.setOnClickListener {
            val countStr = etCount.text?.toString()?.trim()
            val count = countStr?.toIntOrNull()

            if (count == null) {
                etCount.error = getString(R.string.error_number)
                return@setOnClickListener
            }

            val memo = etMemo.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }

            lifecycleScope.launch {
                RecordRepositoryProvider.get(requireContext()).add(count, memo)
                findNavController().navigate(R.id.action_addFragment_to_listFragment)
            }
        }
    }
}
