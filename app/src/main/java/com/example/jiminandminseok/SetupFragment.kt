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
import com.example.jiminandminseok.databinding.FragmentSetupBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SetupFragment : Fragment() {

    private var _binding: FragmentSetupBinding? = null
    private val binding get() = _binding!!

    private val calendar: Calendar = Calendar.getInstance()

    // ✅ 범위
    private val DAILY_MIN = 1
    private val DAILY_MAX = 100

    private val PACK_MIN = 1000
    private val PACK_MAX = 30000

    private val YEARS_MIN = 0
    private val YEARS_MAX = 80

    // ✅ “처음부터 빨간 경고 뜨는거” 방지용
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

        // ✅ 에러 표시 기능 강제 ON (스타일이 덮어도 텍스트는 뜸)
        binding.tilQuitDate.isErrorEnabled = true
        binding.tilDailyCigarettes.isErrorEnabled = true
        binding.tilPackPrice.isErrorEnabled = true
        binding.tilSmokingYears.isErrorEnabled = true

        // ✅ 뒤로가기
        binding.btnBack.setOnClickListener {
            if (!findNavController().navigateUp()) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        // 기본 날짜 표시
        updateDateInView()
        clearAllErrors() // ✅ 처음엔 빨간 글씨 안 보이게

        // 날짜 선택
        binding.etQuitDate.setOnClickListener {
            touchedDate = true
            showDatePicker()
        }

        // 실시간 검증 (입력 건드린 뒤부터만)
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

        // 시작 버튼: 누르면 에러 강제 표시하고 통과하면 저장+이동
        binding.btnStartQuit.setOnClickListener {
            touchedDate = true
            touchedDaily = true
            touchedPack = true
            touchedYears = true

            if (!validateAll(showError = true)) return@setOnClickListener

            val daily = binding.etDailyCigarettes.text.toString().trim().toInt()
            val pack = binding.etPackPrice.text.toString().trim().toInt()
            val years = binding.etSmokingYears.text.toString().trim().toInt()

            val pref = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            with(pref.edit()) {
                putLong("start_date", calendar.timeInMillis)
                putInt("daily_cigarettes", daily)
                putInt("pack_price", pack)
                putInt("smoking_years", years)
                putBoolean("is_setup_complete", true)
                apply()
            }

            findNavController().navigate(R.id.action_setupFragment_to_dashboardFragment)
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

    // ---------- 검증 ----------

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
            binding.tilQuitDate.error = if (ok) null else "미래 날짜는 선택할 수 없어요."
        }
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

        if (showError && touchedDaily) binding.tilDailyCigarettes.error = msg
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

        if (showError && touchedPack) binding.tilPackPrice.error = msg
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
