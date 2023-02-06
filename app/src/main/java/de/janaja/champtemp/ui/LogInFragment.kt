package de.janaja.champtemp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.janaja.champtemp.R

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import de.janaja.champtemp.databinding.FragmentLogInBinding


class LogInFragment : Fragment() {

    private var _binding: FragmentLogInBinding? = null
    private val binding get() = _binding!!

    private val viewModel : TempHumiViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLogInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.authBtnSignin.setOnClickListener { if(checkInput()) viewModel.signIn(binding.authInputEmail.text.toString(), binding.authInputPw.text.toString()) }
        viewModel.currentUser.observe(viewLifecycleOwner){
            if(it != null)
                findNavController().navigate(R.id.action_logInFragment_to_mainFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkInput(): Boolean{
        if(binding.authInputEmail.text.isNullOrEmpty()|| binding.authInputPw.text.isNullOrEmpty()){
            return false
        }
        return true
    }
}