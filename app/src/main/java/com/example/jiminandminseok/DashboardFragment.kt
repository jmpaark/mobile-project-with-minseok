package com.example.jiminandminseok

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

        view.findViewById<View>(R.id.healthGaugeCard).setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_healthRecoveryFragment)
        }

        view.findViewById<View>(R.id.btnQuotes).setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_quotesFragment)
        }

        view.findViewById<View>(R.id.btnSettings).setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_settingsFragment)
        }

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            if (!findNavController().navigateUp()) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        view.findViewById<View>(R.id.btnAddRecord).setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_addFragment)
        }

        view.findViewById<View>(R.id.btnViewRecords).setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_listFragment)
        }

        view.findViewById<View?>(R.id.btnAiQuickTip)?.setOnClickListener {
            view.findViewById<TextView?>(R.id.tvAiResult)?.text = randomTip()
        }

        view.findViewById<View?>(R.id.btnAiSend)?.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_chatFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshDashboard()
    }

    private fun refreshDashboard() {
        viewLifecycleOwner.lifecycleScope.launch {
            val settings = withContext(Dispatchers.IO) {
                DbProvider.get(requireContext().applicationContext)
                    .settingsDao()
                    .get()
            }

            val startDate = settings?.startDate ?: -1L
            val dailyCigs = settings?.dailyCigarettes ?: -1
            val packPrice = settings?.packPrice ?: -1

            val root = requireView()
            val tvTitle = root.findViewById<TextView>(R.id.tvDashboardTitle)
            val tvMoneySaved = root.findViewById<TextView>(R.id.tvMoneySaved)
            val tvLifeRegained = root.findViewById<TextView>(R.id.tvLifeRegained)

            val tvGaugeDesc = root.findViewById<TextView>(R.id.tvHealthGaugeDescription)
            val pbGauge = root.findViewById<ProgressBar>(R.id.healthGaugeBar)
            val tvGaugePct = root.findViewById<TextView>(R.id.tvHealthGaugePercent)

            if (startDate <= 0L || dailyCigs <= 0 || packPrice <= 0) {
                tvTitle.text = getString(R.string.dashboard_day_format, 0)
                tvMoneySaved.text = getString(R.string.dashboard_money_zero)
                tvLifeRegained.text = getString(R.string.dashboard_life_zero)
                tvGaugeDesc.text = getString(R.string.dashboard_missing_settings)
                pbGauge.progress = 0
                tvGaugePct.text = "0%"
                return@launch
            }

            val now = System.currentTimeMillis()
            val days = max(1L, TimeUnit.MILLISECONDS.toDays(now - startDate) + 1)
            tvTitle.text = getString(R.string.dashboard_day_format, days)

            val smokedSinceStart = withContext(Dispatchers.IO) {
                DbProvider.get(requireContext().applicationContext)
                    .recordDao()
                    .sumSmokedSince(startDate)
            }

            val expectedCigs = dailyCigs.toLong() * days
            val notSmokedCigs = (expectedCigs - smokedSinceStart).coerceAtLeast(0L)

            val packs = notSmokedCigs.toDouble() / 20.0
            val moneySaved = (packs * packPrice).toLong()
            tvMoneySaved.text = getString(R.string.dashboard_money_format, formatWon(moneySaved))

            val regainedMinutes = notSmokedCigs * 11L
            tvLifeRegained.text = getString(R.string.dashboard_life_format, formatTime(regainedMinutes))

            val pct = ((days.coerceAtMost(30) * 100) / 30).toInt()
            pbGauge.progress = pct
            tvGaugePct.text = "${pct}%"

            tvGaugeDesc.text = when {
                days < 2 -> getString(R.string.dashboard_gauge_early)
                days < 7 -> getString(R.string.dashboard_gauge_mid)
                days < 30 -> getString(R.string.dashboard_gauge_late)
                else -> getString(R.string.dashboard_gauge_long)
            }
        }
    }

    private fun formatWon(v: Long): String = "%,d".format(v)

    private fun formatTime(minutes: Long): String {
        val h = minutes / 60
        val m = minutes % 60
        val hourUnit = getString(R.string.duration_hour_unit)
        val minUnit = getString(R.string.duration_min_unit)
        return if (h <= 0) "${m}${minUnit}" else "${h}${hourUnit} ${m}${minUnit}"
    }

    private fun randomTip(): String {
        val tips = resources.getStringArray(R.array.ai_quick_tips)
        return tips.random()
    }
}
