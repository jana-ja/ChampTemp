package de.janaja.champtemp.ui.main_content

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import de.janaja.champtemp.adapter.MainViewPagerAdapter
import de.janaja.champtemp.databinding.FragmentMainBinding
import de.janaja.champtemp.ui.TempHumiViewModel

class MainFragment : Fragment() {

    private val viewModel: TempHumiViewModel by activityViewModels()
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!


    private lateinit var mainViewPagerAdapter: MainViewPagerAdapter
    private lateinit var viewPager: ViewPager2


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{

        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewPagerAdapter = MainViewPagerAdapter(this)
        viewPager = binding.mainViewpager
        viewPager.adapter = mainViewPagerAdapter

        TabLayoutMediator(binding.mainTabLayout, viewPager) { _, _ ->
        }.attach()


        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
