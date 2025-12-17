package com.example.jiminandminseok

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.jiminandminseok.databinding.FragmentSetupBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SetupFragment : Fragment() {

    private var _binding: FragmentSetupBinding? = null
    private val binding get() = _binding!!

    private val calendar: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateDateInView()

        binding.etQuitDate.setOnClickListener {
            showDatePicker()
        }

        binding.btnStartQuit.setOnClickListener {
            val dailyCigarettes = binding.etDailyCigarettes.text.toString()
            val packPrice = binding.etPackPrice.text.toString()
            val smokingYears = binding.etSmokingYears.text.toString()

            if (dailyCigarettes.isBlank() || packPrice.isBlank() || smokingYears.isBlank()) {
                Toast.makeText(requireContext(), "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save user data to SharedPreferences
            val sharedPref = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putLong("start_date", calendar.timeInMillis)
                putInt("daily_cigarettes", dailyCigarettes.toInt())
                putInt("pack_price", packPrice.toInt())
                putInt("smoking_years", smokingYears.toInt())
                putBoolean("is_setup_complete", true)
                apply()
            }

            // Navigate to the DashboardFragment
            findNavController().navigate(R.id.action_setupFragment_to_dashboardFragment)
        }
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
