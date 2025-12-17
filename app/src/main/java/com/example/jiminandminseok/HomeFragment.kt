package com.example.jiminandminseok

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.jiminandminseok.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        
        val sharedPref = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val isSetupComplete = sharedPref.getBoolean("is_setup_complete", false)

        if (isSetupComplete) {
            // If setup is complete, immediately navigate to the dashboard
            findNavController().navigate(R.id.action_homeFragment_to_dashboardFragment)
            // Return an empty view, as we are navigating away.
            return View(requireContext())
        }
        
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // This code will only run if setup is NOT complete.
        binding.btnStart.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_setupFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
