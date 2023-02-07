package de.janaja.champtemp.ui.main_content

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.EntryXComparator
import de.janaja.champtemp.R
import de.janaja.champtemp.data.model.TempHumi
import de.janaja.champtemp.databinding.FragmentDayBinding
import de.janaja.champtemp.ui.TempHumiViewModel
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


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

                val chart = binding.chartDay
                chart.description.text = "last 24 hours"

                // entries
                val tempEntries = mutableListOf<Entry>()
                val humiEntries = mutableListOf<Entry>()
                var tempHumiList = it
                if(tempHumiList.size > 24)
                    tempHumiList = tempHumiList.subList(0,24)
                // MPAndroid Chart needs the entries to be sorted by X axis.
                // X axis are the hours. i dont want to have axis from 0 - 23 everytime. so i will map the values of the last 24 to different hours, so that they are "sorted"
                val backTransformMap = getHourBackTransformMap(tempHumiList)
                tempHumiList.forEachIndexed { index, tempHumi ->
                    val time = (tempHumiList.lastIndex - index).toFloat() // tempHumi.timestamp.hour.toFloat()
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
                        return "${if(backTransformMap.containsKey(value.toInt())) backTransformMap[value.toInt()] else -1} Uhr"
                    }
                }
                val xAxis: XAxis = chart.xAxis
                xAxis.valueFormatter = formatter
                val tempYAxis: YAxis = chart.axisLeft
                tempYAxis.axisMaximum = 20f
                tempYAxis.axisMinimum = 10f
                val humiYAxis: YAxis = chart.axisRight
                humiYAxis.axisMaximum = 55f
                humiYAxis.axisMinimum = 40f

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

    private fun getHourBackTransformMap(tempHumiList: List<TempHumi>): Map<Int, Int> {
        val hourBackMap = HashMap<Int,Int>()
        // X axis are the hours. so i will map the values of the last 24 to different hours, so that they are "sorted"
        // f.e. its 14 so i want to show data from yesterday 15 until now 14
        // 15 -> 0, 16 -> 1...
        // oldest hour needs to be 0
        // newest hour needs to be 23 (or max index)
        for (i in tempHumiList.indices){
            hourBackMap[tempHumiList.lastIndex - i] = tempHumiList[i].timestamp.hour
        }
        return hourBackMap
    }
}