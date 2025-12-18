package com.example.jiminandminseok

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class HealthRecoveryFragment : Fragment(R.layout.fragment_health_recovery) {

    private data class Milestone(
        val minutes: Long,
        val timeLabel: String,
        val title: String,
        val detail: String
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            if (!findNavController().navigateUp()) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val settings = withContext(Dispatchers.IO) {
                DbProvider.get(requireContext().applicationContext)
                    .settingsDao()
                    .get()
            }

            val startDate = settings?.startDate ?: -1L
            val now = System.currentTimeMillis()
            val elapsedMillis = if (startDate > 0L) (now - startDate).coerceAtLeast(0L) else 0L
            val elapsedMinutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis)

            val milestones = buildMilestones()
            val totalMinutes = milestones.lastOrNull()?.minutes ?: 1L
            val pct = ((elapsedMinutes * 100) / totalMinutes).toInt().coerceIn(0, 100)

            view.findViewById<TextView>(R.id.tvQuitDuration).text =
                getString(R.string.health_elapsed_format, formatDuration(elapsedMinutes))
            view.findViewById<ProgressBar>(R.id.healthProgressBar).progress = pct
            view.findViewById<TextView>(R.id.tvGaugePercent).text = "${pct}%"

            val next = milestones.firstOrNull { it.minutes > elapsedMinutes }
            val nextText = if (next == null) {
                getString(R.string.health_next_all)
            } else {
                getString(R.string.health_next_format, next.timeLabel, next.title)
            }
            view.findViewById<TextView>(R.id.tvNextMilestone).text = nextText

            val container = view.findViewById<LinearLayout>(R.id.milestoneContainer)
            val inflater = LayoutInflater.from(requireContext())
            container.removeAllViews()

            for (milestone in milestones) {
                val item = inflater.inflate(R.layout.item_health_milestone, container, false)
                item.findViewById<TextView>(R.id.tvMilestoneTime).text = milestone.timeLabel
                item.findViewById<TextView>(R.id.tvMilestoneTitle).text = milestone.title

                val unlocked = elapsedMinutes >= milestone.minutes
                item.findViewById<TextView>(R.id.tvMilestoneDetail).text = milestone.detail

                val statusView = item.findViewById<TextView>(R.id.tvMilestoneStatus)
                statusView.text = if (unlocked) {
                    getString(R.string.health_status_done)
                } else {
                    getString(R.string.health_status_upcoming)
                }
                statusView.setTextColor(
                    if (unlocked) {
                        ContextCompat.getColor(requireContext(), R.color.primary_mint_green)
                    } else {
                        ContextCompat.getColor(requireContext(), R.color.text_secondary_light)
                    }
                )

                container.addView(item)
            }
        }
    }

    private fun buildMilestones(): List<Milestone> {
        val minutes = listOf(
            20L,
            8 * 60L,
            12 * 60L,
            24 * 60L,
            48 * 60L,
            72 * 60L,
            14 * 24 * 60L,
            30 * 24 * 60L,
            90 * 24 * 60L,
            180 * 24 * 60L,
            365 * 24 * 60L,
            5 * 365 * 24 * 60L,
            10 * 365 * 24 * 60L,
            15 * 365 * 24 * 60L
        )

        val times = resources.getStringArray(R.array.health_milestone_times)
        val titles = resources.getStringArray(R.array.health_milestone_titles)
        val details = resources.getStringArray(R.array.health_milestone_details)

        return minutes.indices.map { index ->
            Milestone(minutes[index], times[index], titles[index], details[index])
        }
    }

    private fun formatDuration(totalMinutes: Long): String {
        if (totalMinutes <= 0L) return "0${getString(R.string.duration_min_unit)}"
        val days = totalMinutes / (24 * 60)
        val hours = (totalMinutes % (24 * 60)) / 60
        val minutes = totalMinutes % 60

        val dayUnit = getString(R.string.duration_day_unit)
        val hourUnit = getString(R.string.duration_hour_unit)
        val minUnit = getString(R.string.duration_min_unit)

        return when {
            days > 0 -> "${days}${dayUnit} ${hours}${hourUnit}"
            hours > 0 -> "${hours}${hourUnit} ${minutes}${minUnit}"
            else -> "${minutes}${minUnit}"
        }
    }
}
