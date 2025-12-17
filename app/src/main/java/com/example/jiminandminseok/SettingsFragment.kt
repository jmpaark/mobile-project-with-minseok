package com.example.jiminandminseok

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.jiminandminseok.databinding.FragmentSettingsBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val calendar: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUserData()

        binding.etQuitDate.setOnClickListener {
            showDatePicker()
        }

        binding.btnSaveChanges.setOnClickListener {
            saveUserData()
        }
    }

    private fun loadUserData() {
        val sharedPref = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val startDate = sharedPref.getLong("start_date", System.currentTimeMillis())
        val dailyCigarettes = sharedPref.getInt("daily_cigarettes", 0)
        val packPrice = sharedPref.getInt("pack_price", 0)
        val smokingYears = sharedPref.getInt("smoking_years", 0)

        calendar.timeInMillis = startDate
        updateDateInView()

        binding.etDailyCigarettes.setText(dailyCigarettes.toString())
        binding.etPackPrice.setText(packPrice.toString())
        binding.etSmokingYears.setText(smokingYears.toString())
    }

    private fun saveUserData() {
        val dailyCigarettes = binding.etDailyCigarettes.text.toString()
        val packPrice = binding.etPackPrice.text.toString()
        val smokingYears = binding.etSmokingYears.text.toString()

        if (dailyCigarettes.isBlank() || packPrice.isBlank() || smokingYears.isBlank()) {
            Toast.makeText(requireContext(), "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPref = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putLong("start_date", calendar.timeInMillis)
            putInt("daily_cigarettes", dailyCigarettes.toInt())
            putInt("pack_price", packPrice.toInt())
            putInt("smoking_years", smokingYears.toInt())
            apply()
        }

        Toast.makeText(requireContext(), "변경사항이 성공적으로 저장되었습니다.", Toast.LENGTH_SHORT).show()
    }

    private fun showDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }

        DatePickerDialog(
            requireContext(),
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateInView() {
        val myFormat = "yyyy/MM/dd"
        val sdf = SimpleDateFormat(myFormat, Locale.KOREA)
        binding.etQuitDate.setText(sdf.format(calendar.time))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
