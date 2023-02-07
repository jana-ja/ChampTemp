package de.janaja.champtemp.ui.main_content

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import de.janaja.champtemp.R
import de.janaja.champtemp.databinding.FragmentHomeBinding
import de.janaja.champtemp.ui.TempHumiViewModel

class HomeFragment : Fragment() {

    private val viewModel: TempHumiViewModel by activityViewModels()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getAll()
        viewModel.tempHumis.observe(viewLifecycleOwner){
            if(it.isNotEmpty()){
                binding.tvDegreesNow.text = getString(R.string.degrees_now, it[0].temp)
                binding.tvHumiNow.text = getString(R.string.humi_now, it[0].humi)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}