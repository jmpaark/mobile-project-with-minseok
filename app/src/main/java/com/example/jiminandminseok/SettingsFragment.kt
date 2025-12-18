package com.example.jiminandminseok

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.jiminandminseok.databinding.FragmentSettingsBinding
import java.text.SimpleDateFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val calendar: Calendar = Calendar.getInstance()

    private val DAILY_MIN = 1
    private val DAILY_MAX = 100

    private val PACK_MIN = 1000
    private val PACK_MAX = 30000

    private val YEARS_MIN = 0
    private val YEARS_MAX = 80

    private var triedSubmit = false
    private var touchedDaily = false
    private var touchedPack = false
    private var touchedYears = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            if (!findNavController().navigateUp()) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        loadUserData()
        clearAllErrors()

        binding.etQuitDate.setOnClickListener { showDatePicker() }

        binding.etDailyCigarettes.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                touchedDaily = true
                validateDaily(showError = true)
            }
        }
        binding.etPackPrice.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                touchedPack = true
                validatePack(showError = true)
            }
        }
        binding.etSmokingYears.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                touchedYears = true
                validateYears(showError = true)
            }
        }

        binding.etDailyCigarettes.addTextChangedListener(simpleWatcher {
            if (touchedDaily || triedSubmit) validateDaily(showError = true) else validateDaily(showError = false)
        })
        binding.etPackPrice.addTextChangedListener(simpleWatcher {
            if (touchedPack || triedSubmit) validatePack(showError = true) else validatePack(showError = false)
        })
        binding.etSmokingYears.addTextChangedListener(simpleWatcher {
            if (touchedYears || triedSubmit) validateYears(showError = true) else validateYears(showError = false)
        })

        binding.btnSaveChanges.setOnClickListener {
            triedSubmit = true
            val ok =
                validateQuitDate(showError = true) &&
                    validateDaily(showError = true) &&
                    validatePack(showError = true) &&
                    validateYears(showError = true)

            if (!ok) return@setOnClickListener

            saveUserData()
            Toast.makeText(requireContext(), getString(R.string.settings_saved), Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserData() {
        viewLifecycleOwner.lifecycleScope.launch {
            val data = withContext(Dispatchers.IO) {
                DbProvider.get(requireContext().applicationContext)
                    .settingsDao()
                    .get()
            }

            val startDate = data?.startDate ?: 0L
            val dailyCigarettes = data?.dailyCigarettes ?: 0
            val packPrice = data?.packPrice ?: 0
            val smokingYears = data?.smokingYears ?: 0

            calendar.timeInMillis = if (startDate > 0L) startDate else System.currentTimeMillis()
            updateDateInView()

            binding.etDailyCigarettes.setText(if (dailyCigarettes == 0) "" else dailyCigarettes.toString())
            binding.etPackPrice.setText(if (packPrice == 0) "" else packPrice.toString())
            binding.etSmokingYears.setText(if (smokingYears == 0) "" else smokingYears.toString())
        }
    }

    private fun saveUserData() {
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
        }
    }

    private fun showDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            updateDateInView()
            if (triedSubmit) validateQuitDate(showError = true) else validateQuitDate(showError = false)
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
        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.KOREA)
        binding.etQuitDate.setText(sdf.format(calendar.time))
    }

    private fun clearAllErrors() {
        binding.tilQuitDate.error = null
        binding.tilDailyCigarettes.error = null
        binding.tilPackPrice.error = null
        binding.tilSmokingYears.error = null
    }

    private fun validateQuitDate(showError: Boolean): Boolean {
        val now = System.currentTimeMillis()
        val ok = calendar.timeInMillis <= now
        binding.tilQuitDate.error = if (!ok && showError) getString(R.string.error_future_date) else null
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

        binding.tilDailyCigarettes.error = if (!ok && showError) msg else null
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

        binding.tilPackPrice.error = if (!ok && showError) msg else null
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

        binding.tilSmokingYears.error = if (!ok && showError) msg else null
        return ok
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
