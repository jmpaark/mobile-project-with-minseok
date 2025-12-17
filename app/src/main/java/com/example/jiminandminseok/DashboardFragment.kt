package com.example.jiminandminseok

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.btnAddRecord).setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_addFragment)
        }

        view.findViewById<View>(R.id.btnViewRecords).setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_listFragment)
        }
    }
}
