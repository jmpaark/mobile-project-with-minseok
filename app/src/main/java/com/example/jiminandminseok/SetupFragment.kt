package com.example.jiminandminseok

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.jiminandminseok.databinding.FragmentSetupBinding
import java.text.SimpleDateFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale

class SetupFragment : Fragment() {

    private var _binding: FragmentSetupBinding? = null
    private val binding get() = _binding!!

    private val calendar: Calendar = Calendar.getInstance()

    private val DAILY_MIN = 1
    private val DAILY_MAX = 100

    private val PACK_MIN = 1000
    private val PACK_MAX = 30000

    private val YEARS_MIN = 0
    private val YEARS_MAX = 80

    private var touchedDaily = false
    private var touchedPack = false
    private var touchedYears = false
    private var touchedDate = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tilQuitDate.isErrorEnabled = true
        binding.tilDailyCigarettes.isErrorEnabled = true
        binding.tilPackPrice.isErrorEnabled = true
        binding.tilSmokingYears.isErrorEnabled = true

        binding.btnBack.setOnClickListener {
            if (!findNavController().navigateUp()) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        updateDateInView()
        clearAllErrors()

        binding.etQuitDate.setOnClickListener {
            touchedDate = true
            showDatePicker()
        }

        binding.etDailyCigarettes.addTextChangedListener(simpleWatcher {
            touchedDaily = true
            validateDaily(showError = true)
        })

        binding.etPackPrice.addTextChangedListener(simpleWatcher {
            touchedPack = true
            validatePack(showError = true)
        })

        binding.etSmokingYears.addTextChangedListener(simpleWatcher {
            touchedYears = true
            validateYears(showError = true)
        })

        binding.btnStartQuit.setOnClickListener {
            touchedDate = true
            touchedDaily = true
            touchedPack = true
            touchedYears = true

            if (!validateAll(showError = true)) return@setOnClickListener

            val daily = binding.etDailyCigarettes.text.toString().trim().toInt()
            val pack = binding.etPackPrice.text.toString().trim().toInt()
            val years = binding.etSmokingYears.text.toString().trim().toInt()

            viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    DbProvider.get(requireContext().applicationContext)
                        .settingsDao()
                        .upsert(
                            UserSettingsEntity(
                                startDate = calendar.timeInMillis,
                                dailyCigarettes = daily,
                                packPrice = pack,
                                smokingYears = years,
                                isSetupComplete = true
                            )
                        )
                }

                findNavController().navigate(R.id.action_setupFragment_to_dashboardFragment)
            }
        }
    }

    private fun showDatePicker() {
        val listener = DatePickerDialog.OnDateSetListener { _, y, m, d ->
            calendar.set(Calendar.YEAR, y)
            calendar.set(Calendar.MONTH, m)
            calendar.set(Calendar.DAY_OF_MONTH, d)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            updateDateInView()
            validateQuitDate(showError = true)
        }

        DatePickerDialog(
            requireContext(),
            listener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateInView() {
        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.KOREA)
        binding.etQuitDate.setText(sdf.format(calendar.time))
    }

    private fun validateAll(showError: Boolean): Boolean {
        val a = validateQuitDate(showError)
        val b = validateDaily(showError)
        val c = validatePack(showError)
        val d = validateYears(showError)
        return a && b && c && d
    }

    private fun validateQuitDate(showError: Boolean): Boolean {
        val ok = calendar.timeInMillis <= System.currentTimeMillis()
        if (showError && touchedDate) {
            binding.tilQuitDate.error = if (ok) null else getString(R.string.error_future_date)
        }
        return ok
    }

    private fun validateDaily(showError: Boolean): Boolean {
        val s = binding.etDailyCigarettes.text?.toString()?.trim().orEmpty()
        val v = s.toIntOrNull()

        val (ok, msg) = when {
            s.isEmpty() -> false to getString(R.string.error_required)
            v == null -> false to getString(R.string.error_number)
            v < DAILY_MIN -> false to getString(R.string.error_min_format, DAILY_MIN)
            v > DAILY_MAX -> false to getString(R.string.error_max_format, DAILY_MAX)
            else -> true to null
        }

        if (showError && touchedDaily) binding.tilDailyCigarettes.error = msg
        return ok
    }

    private fun validatePack(showError: Boolean): Boolean {
        val s = binding.etPackPrice.text?.toString()?.trim().orEmpty()
        val v = s.toIntOrNull()

        val (ok, msg) = when {
            s.isEmpty() -> false to getString(R.string.error_required)
            v == null -> false to getString(R.string.error_number)
            v < PACK_MIN -> false to getString(R.string.error_min_format, PACK_MIN)
            v > PACK_MAX -> false to getString(R.string.error_max_format, PACK_MAX)
            else -> true to null
        }

        if (showError && touchedPack) binding.tilPackPrice.error = msg
        return ok
    }

    private fun validateYears(showError: Boolean): Boolean {
        val s = binding.etSmokingYears.text?.toString()?.trim().orEmpty()
        val v = s.toIntOrNull()

        val (ok, msg) = when {
            s.isEmpty() -> false to getString(R.string.error_required)
            v == null -> false to getString(R.string.error_number)
            v < YEARS_MIN -> false to getString(R.string.error_min_format, YEARS_MIN)
            v > YEARS_MAX -> false to getString(R.string.error_max_format, YEARS_MAX)
            else -> true to null
        }

        if (showError && touchedYears) binding.tilSmokingYears.error = msg
        return ok
    }

    private fun clearAllErrors() {
        binding.tilQuitDate.error = null
        binding.tilDailyCigarettes.error = null
        binding.tilPackPrice.error = null
        binding.tilSmokingYears.error = null
    }

    private fun simpleWatcher(onChange: () -> Unit): TextWatcher =
        object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { onChange() }
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
