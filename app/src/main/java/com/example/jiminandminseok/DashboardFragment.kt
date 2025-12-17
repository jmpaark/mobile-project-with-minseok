package com.example.jiminandminseok

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.jiminandminseok.databinding.FragmentDashboardBinding
import java.util.concurrent.TimeUnit

data class HealthBenefit(val timeMillis: Long, val description: String)

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    // Health benefits milestones (in milliseconds)
    private val healthBenefits = listOf(
        HealthBenefit(TimeUnit.MINUTES.toMillis(20), "혈압과 맥박이 정상으로 돌아옵니다."),
        HealthBenefit(TimeUnit.HOURS.toMillis(8), "혈중 산소 농도가 정상으로 돌아옵니다."),
        HealthBenefit(TimeUnit.HOURS.toMillis(24), "심장마비 위험이 감소하기 시작합니다."),
        HealthBenefit(TimeUnit.DAYS.toMillis(2), "신경 말단 기능이 회복되기 시작합니다."),
        HealthBenefit(TimeUnit.DAYS.toMillis(14), "혈액순환이 개선되고 걷기가 더 쉬워집니다."),
        HealthBenefit(TimeUnit.DAYS.toMillis(30), "폐 기능이 30% 이상 향상됩니다."),
        HealthBenefit(TimeUnit.DAYS.toMillis(270), "폐의 자체 정화 기능이 정상화됩니다."),
        HealthBenefit(TimeUnit.DAYS.toMillis(365), "심장병 위험이 흡연자의 절반으로 줄어듭니다."),
        HealthBenefit(TimeUnit.YEARS.toMillis(5), "심장마비 위험이 비흡연자와 거의 같아집니다."),
        HealthBenefit(TimeUnit.YEARS.toMillis(10), "폐암 사망률이 흡연자의 절반 수준으로 감소합니다.")
    ).sortedBy { it.timeMillis }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateDashboard()
    }

    private fun updateDashboard() {
        val sharedPref = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val startDate = sharedPref.getLong("start_date", 0)
        val dailyCigarettes = sharedPref.getInt("daily_cigarettes", 0)
        val packPrice = sharedPref.getInt("pack_price", 0)

        val timeSinceQuit = System.currentTimeMillis() - startDate
        val daysSince = TimeUnit.MILLISECONDS.toDays(timeSinceQuit)

        // --- Main Stats ---
        val pricePerCigarette = packPrice / 20.0
        val cigarettesNotSmoked = (timeSinceQuit / (1000.0 * 60 * 60 * 24) * dailyCigarettes).toInt()
        val moneySaved = (cigarettesNotSmoked * pricePerCigarette).toInt()
        val lifeRegainedMinutes = cigarettesNotSmoked * 11L
        val lifeRegainedDays = TimeUnit.MINUTES.toDays(lifeRegainedMinutes)

        binding.tvDashboardTitle.text = "금연 ${daysSince + 1}일차"
        binding.tvMoneySaved.text = "%,d원".format(moneySaved)
        binding.tvLifeRegained.text = "+ %d일".format(lifeRegainedDays)

        // --- Health Recovery Gauge ---
        updateHealthGauge(timeSinceQuit)

        // --- Badges ---
        setupBadges(daysSince)
    }

    private fun updateHealthGauge(timeSinceQuit: Long) {
        var previousBenefit: HealthBenefit? = null
        var nextBenefit: HealthBenefit? = null
        for (benefit in healthBenefits) {
            if (timeSinceQuit < benefit.timeMillis) {
                nextBenefit = benefit
                break
            }
            previousBenefit = benefit
        }

        if (nextBenefit != null) {
            val startTime = previousBenefit?.timeMillis ?: 0
            val endTime = nextBenefit.timeMillis
            val progress = ((timeSinceQuit - startTime).toFloat() / (endTime - startTime).toFloat() * 100).toInt()
            
            binding.tvHealthGaugeDescription.text = nextBenefit.description
            binding.healthGaugeBar.progress = progress
            binding.tvHealthGaugePercent.text = "$progress%"
        } else {
            // All benefits achieved
            binding.tvHealthGaugeDescription.text = "모든 건강 목표를 달성하셨습니다!"
            binding.healthGaugeBar.progress = 100
            binding.tvHealthGaugePercent.text = "100%"
        }
    }

    private fun setupBadges(days: Long) {
        binding.badgesContainer.removeAllViews()
        val badges = listOf(
            Pair(1, "1일 달성"),
            Pair(3, "3일 달성"),
            Pair(7, "1주일 달성"),
            Pair(30, "1개월 달성"),
            Pair(100, "100일 달성"),
            Pair(365, "1주년!")
        )

        for (badge in badges) {
            val achieved = days + 1 >= badge.first
            val badgeView = createBadgeView(badge.second, achieved)
            binding.badgesContainer.addView(badgeView)
        }
    }

    private fun createBadgeView(text: String, achieved: Boolean): View {
        val inflater = LayoutInflater.from(requireContext())
        val badgeLayout = inflater.inflate(R.layout.item_badge, binding.badgesContainer, false) as LinearLayout
        val badgeIcon = badgeLayout.findViewById<ImageView>(R.id.ivBadgeIcon)
        val badgeText = badgeLayout.findViewById<TextView>(R.id.tvBadgeText)

        badgeText.text = text
        if (achieved) {
            badgeIcon.setImageResource(R.drawable.ic_badge_unlocked)
            badgeText.setTextColor(ContextCompat.getColor(requireContext(), R.color.accent_coral_orange))
        } else {
            badgeIcon.setImageResource(R.drawable.ic_badge_locked)
            badgeText.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary_light))
        }
        return badgeLayout
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
