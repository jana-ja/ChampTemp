package de.janaja.champtemp.ui.main_content

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.EntryXComparator
import de.janaja.champtemp.R
import de.janaja.champtemp.databinding.FragmentDayBinding
import de.janaja.champtemp.ui.TempHumiViewModel
import java.lang.Math.random
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random


class DayFragment : Fragment() {

    private val viewModel: TempHumiViewModel by activityViewModels()
    private var _binding: FragmentDayBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        _binding = FragmentDayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.tempHumis.observe(viewLifecycleOwner){
            if(it.isNotEmpty()) {

                val chart = binding.chart
                chart.description = Description()

                // entries
                val tempEntries = mutableListOf<Entry>()
                val humiEntries = mutableListOf<Entry>()
                var tempHumiList = it
                if(tempHumiList.size > 24)
                    tempHumiList = tempHumiList.subList(0,24)
                tempHumiList.forEach { tempHumi ->
                    val time = tempHumi.timestamp.hour.toFloat()
                    tempEntries.add(Entry(time, tempHumi.temp.toFloat()))
                    humiEntries.add(Entry(time, tempHumi.humi.toFloat()))
                }
                Collections.sort(tempEntries, EntryXComparator())
                Collections.sort(humiEntries, EntryXComparator())

                // data sets
                val tempDataSet = LineDataSet(tempEntries, "Temperature")
                tempDataSet.axisDependency = YAxis.AxisDependency.LEFT
                val tempColor = resources.getColor(R.color.champignon_5)
                tempDataSet.color = tempColor
                tempDataSet.valueTextColor = tempColor
                tempDataSet.setCircleColor(tempColor)
                val humiDataSet = LineDataSet(humiEntries, "Humidity")
                humiDataSet.axisDependency = YAxis.AxisDependency.RIGHT
                val humiColor = resources.getColor(R.color.champignon_4)
                humiDataSet.color = humiColor
                humiDataSet.valueTextColor = humiColor
                humiDataSet.setCircleColor(humiColor)

                // combine datasets
                val dataSets: MutableList<ILineDataSet> = ArrayList()
                dataSets.add(tempDataSet)
                dataSets.add(humiDataSet)

                // x axis description
                //val days = arrayOf("Son", "Mon", "Din", "Mit", "Don", "Fre", "Sam")
                val formatter: ValueFormatter = object : ValueFormatter() {
                    override fun getAxisLabel(value: Float, axis: AxisBase): String {
                        return "${value.toInt()} Uhr"
                    }
                }
                val xAxis: XAxis = chart.xAxis
                //xAxis.granularity = 1f // minimum axis-step (interval) is 1
                xAxis.valueFormatter = formatter

                // line data
                val lineData = LineData(dataSets)
                chart.data = lineData
                chart.invalidate()
            }
        }






    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}