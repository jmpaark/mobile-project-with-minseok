package com.example.jiminandminseok

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.jiminandminseok.databinding.FragmentSettingsBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val calendar: Calendar = Calendar.getInstance()

    // ✅ 검증 범위 (원하면 여기 숫자만 바꿔)
    private val DAILY_MIN = 1
    private val DAILY_MAX = 100

    private val PACK_MIN = 1000
    private val PACK_MAX = 30000

    private val YEARS_MIN = 0
    private val YEARS_MAX = 80

    // ✅ “처음 실행할 때 빨간 경고 안 뜨게”용
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

        // ✅ 뒤로가기
        binding.btnBack.setOnClickListener {
            if (!findNavController().navigateUp()) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        loadUserData()
        clearAllErrors() // ⭐ 처음엔 경고 안 뜨게

        // 날짜 선택
        binding.etQuitDate.setOnClickListener { showDatePicker() }

        // 터치 여부 감지(포커스 한번이라도 갔다오면 touched=true)
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

        // 입력 중 실시간 검증(단, touched 이후에만 빨간 경고)
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
            Toast.makeText(requireContext(), "변경사항이 성공적으로 저장되었습니다.", Toast.LENGTH_SHORT).show()
            // 원하면 저장 후 자동 뒤로가기:
            // findNavController().navigateUp()
        }
    }

    private fun loadUserData() {
        val sharedPref = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        val startDate = sharedPref.getLong("start_date", 0L)
        val dailyCigarettes = sharedPref.getInt("daily_cigarettes", 0)
        val packPrice = sharedPref.getInt("pack_price", 0)
        val smokingYears = sharedPref.getInt("smoking_years", 0)

        // 날짜 표시(없으면 오늘 표시)
        calendar.timeInMillis = if (startDate > 0L) startDate else System.currentTimeMillis()
        updateDateInView()

        // 0은 빈칸으로 보이게
        binding.etDailyCigarettes.setText(if (dailyCigarettes == 0) "" else dailyCigarettes.toString())
        binding.etPackPrice.setText(if (packPrice == 0) "" else packPrice.toString())
        binding.etSmokingYears.setText(if (smokingYears == 0) "" else smokingYears.toString())
    }

    private fun saveUserData() {
        val daily = binding.etDailyCigarettes.text.toString().trim().toInt()
        val pack = binding.etPackPrice.text.toString().trim().toInt()
        val years = binding.etSmokingYears.text.toString().trim().toInt()

        val sharedPref = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putLong("start_date", calendar.timeInMillis)
            putInt("daily_cigarettes", daily)
            putInt("pack_price", pack)
            putInt("smoking_years", years)
            apply()
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

    // ---------- 검증 ----------

    private fun validateQuitDate(showError: Boolean): Boolean {
        val now = System.currentTimeMillis()
        val ok = calendar.timeInMillis <= now
        binding.tilQuitDate.error = if (!ok && showError) "미래 날짜는 선택할 수 없어요." else null
        return ok
    }

    private fun validateDaily(showError: Boolean): Boolean {
        val s = binding.etDailyCigarettes.text?.toString()?.trim().orEmpty()
        val v = s.toIntOrNull()

        val (ok, msg) = when {
            s.isEmpty() -> false to "하루 평균 흡연량을 입력해주세요."
            v == null -> false to "숫자만 입력해주세요."
            v < DAILY_MIN -> false to "${DAILY_MIN} 이상으로 입력해주세요."
            v > DAILY_MAX -> false to "하루 ${DAILY_MAX}개비 초과는 비현실적이에요."
            else -> true to null
        }

        binding.tilDailyCigarettes.error = if (!ok && showError) msg else null
        return ok
    }

    private fun validatePack(showError: Boolean): Boolean {
        val s = binding.etPackPrice.text?.toString()?.trim().orEmpty()
        val v = s.toIntOrNull()

        val (ok, msg) = when {
            s.isEmpty() -> false to "담배 한 갑 가격을 입력해주세요."
            v == null -> false to "숫자만 입력해주세요."
            v < PACK_MIN -> false to "${PACK_MIN}원 미만은 비현실적이에요."
            v > PACK_MAX -> false to "${PACK_MAX}원 초과는 장난치는 값으로 보여요."
            else -> true to null
        }

        binding.tilPackPrice.error = if (!ok && showError) msg else null
        return ok
    }

    private fun validateYears(showError: Boolean): Boolean {
        val s = binding.etSmokingYears.text?.toString()?.trim().orEmpty()
        val v = s.toIntOrNull()

        val (ok, msg) = when {
            s.isEmpty() -> false to "총 흡연 기간(년)을 입력해주세요."
            v == null -> false to "숫자만 입력해주세요."
            v < YEARS_MIN -> false to "${YEARS_MIN} 이상으로 입력해주세요."
            v > YEARS_MAX -> false to "${YEARS_MAX}년 초과는 비현실적이에요."
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
