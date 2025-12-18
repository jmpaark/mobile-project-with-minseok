package com.example.jiminandminseok

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import kotlin.math.max

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 기록 추가
        view.findViewById<View>(R.id.btnAddRecord).setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_addFragment)
        }

        // 기록 보기
        view.findViewById<View>(R.id.btnViewRecords).setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_listFragment)
        }

        // 한줄 조언
        view.findViewById<View?>(R.id.btnAiQuickTip)?.setOnClickListener {
            view.findViewById<TextView?>(R.id.tvAiResult)?.text = randomTip()
        }

        // 상담하기
        view.findViewById<View?>(R.id.btnAiSend)?.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_chatFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshDashboard()
    }

    private fun refreshDashboard() {
        val pref = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        val startDate = pref.getLong("start_date", -1L)
        val dailyCigs = pref.getInt("daily_cigarettes", -1)
        val packPrice = pref.getInt("pack_price", -1)

        val root = requireView()
        val tvTitle = root.findViewById<TextView>(R.id.tvDashboardTitle)
        val tvMoneySaved = root.findViewById<TextView>(R.id.tvMoneySaved)
        val tvLifeRegained = root.findViewById<TextView>(R.id.tvLifeRegained)

        val tvGaugeDesc = root.findViewById<TextView>(R.id.tvHealthGaugeDescription)
        val pbGauge = root.findViewById<ProgressBar>(R.id.healthGaugeBar)
        val tvGaugePct = root.findViewById<TextView>(R.id.tvHealthGaugePercent)

        // 값 미설정/이상값이면 안내
        if (startDate <= 0L || dailyCigs <= 0 || packPrice <= 0) {
            tvTitle.text = "금연 ?일차"
            tvMoneySaved.text = "0원"
            tvLifeRegained.text = "+ 0분"
            tvGaugeDesc.text = "금연 정보가 설정되지 않았습니다. (설정/초기화)에서 입력해보세요."
            pbGauge.progress = 0
            tvGaugePct.text = "0%"
            return
        }

        val now = System.currentTimeMillis()
        val days = max(1L, TimeUnit.MILLISECONDS.toDays(now - startDate) + 1)
        tvTitle.text = "금연 ${days}일차"

        viewLifecycleOwner.lifecycleScope.launch {
            // ✅ DB에서 “실제로 핀 개비” 합계 가져오기
            val smokedSinceStart = withContext(Dispatchers.IO) {
                DbProvider.get(requireContext().applicationContext)
                    .recordDao()
                    .sumSmokedSince(startDate)
            }

            // ✅ 기대 흡연량(원래 피웠을 개비)
            val expectedCigs = dailyCigs.toLong() * days

            // ✅ 실제 핀 개비만큼 절약이 줄어듦
            val notSmokedCigs = (expectedCigs - smokedSinceStart).coerceAtLeast(0L)

            // ✅ 절약 금액 = (안 핀 개비 / 20갑) * 갑가격
            val packs = notSmokedCigs.toDouble() / 20.0
            val moneySaved = (packs * packPrice).toLong()
            tvMoneySaved.text = "${formatWon(moneySaved)}원"

            // ✅ 되찾은 수명(1개비=11분 가정)도 “안 핀 개비” 기준
            val regainedMinutes = notSmokedCigs * 11L
            tvLifeRegained.text = "+ ${formatTime(regainedMinutes)}"

            // 건강 게이지(30일=100%)
            val pct = ((days.coerceAtMost(30) * 100) / 30).toInt()
            pbGauge.progress = pct
            tvGaugePct.text = "${pct}%"

            tvGaugeDesc.text = when {
                days < 2 -> "혈압과 맥박이 정상으로 돌아옵니다"
                days < 7 -> "혈중 산소 수치가 개선됩니다"
                days < 30 -> "폐 기능이 서서히 회복됩니다"
                else -> "장기적인 건강 회복 단계에 들어섰어요"
            }
        }
    }

    private fun formatWon(v: Long): String = "%,d".format(v)

    private fun formatTime(minutes: Long): String {
        val h = minutes / 60
        val m = minutes % 60
        return if (h <= 0) "${m}분" else "${h}시간 ${m}분"
    }

    private fun randomTip(): String {
        val tips = listOf(
            "지금은 물 한 컵 + 3분만 버텨봐. 욕구는 금방 꺾인다.",
            "양치하면 ‘담배 생각’이 확 줄어. 바로 해보자.",
            "지금 밖에 나가서 1분만 걷자. 루틴 끊기가 핵심.",
            "한 대만은 다시 시작이야. 오늘 버틴 게 아까워!"
        )
        return tips.random()
    }
}
