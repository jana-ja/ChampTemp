package de.janaja.champtemp.ui.main_content

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import de.janaja.champtemp.databinding.FragmentWeekBinding
import de.janaja.champtemp.ui.TempHumiViewModel
import java.util.*
import kotlin.collections.ArrayList

class WeekFragment : Fragment() {

    private val viewModel: TempHumiViewModel by activityViewModels()
    private var _binding: FragmentWeekBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        _binding = FragmentWeekBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.tempHumis.observe(viewLifecycleOwner){
            if(it.isNotEmpty()) {

                val chart = binding.chartWeek
                chart.description.text = "last 7 days"

                // entries
                val tempEntries = mutableListOf<Entry>()
                val humiEntries = mutableListOf<Entry>()
                var tempHumiList = it
                if(tempHumiList.size > 24*6)
                    tempHumiList = tempHumiList.subList(0,24*6)
                // MPAndroid Chart needs the entries to be sorted by X axis.
                // X axis are the hours. i dont want to have axis from 0 - 23 everytime. so i will map the values of the last 24 to different hours, so that they are "sorted"

                val dayBackMap = HashMap<Int,Int>()
                var nextAvg = false
                var day: Int = tempHumiList[0].timestamp.dayOfWeek.value
                var currentAvgTemp = tempHumiList[0].temp
                var currentAvgHumi = tempHumiList[0].humi
                var avgCounter = 0
                var i = 0
                var xLabel = (tempHumiList.size / 24) + 1 - i //bei 1 tag -> 0, 2 tage -> 1 usw
                dayBackMap[xLabel] = day
                tempHumiList.subList(1, tempHumiList.size).forEach { tempHumi ->
                    if(nextAvg){
                        // begin of new avg
                        nextAvg = false
                        day = tempHumi.timestamp.dayOfWeek.value
                        currentAvgTemp = tempHumi.temp
                        currentAvgHumi = tempHumi.humi
                        avgCounter = 0
                        i++ // 0, 1, 2, 3, 4, 5, 6
                        xLabel = (tempHumiList.size / 24) + 1 - i // 6, 5, 4, 3, 2, 1, 0 - 7 tage
                        dayBackMap[xLabel] = day
                    }
                    if(tempHumi.timestamp.dayOfWeek.value == day){
                        // belongs to the current avg
                        currentAvgTemp += tempHumi.temp
                        currentAvgHumi += tempHumi.humi
                        avgCounter++
                    } else {
                        // create entry and begin new avg values
                        val avgTemp = currentAvgTemp.toFloat() / avgCounter
                        val avgHumi = currentAvgHumi.toFloat() / avgCounter
                        tempEntries.add(Entry(xLabel.toFloat(), avgTemp))
                        humiEntries.add(Entry(xLabel.toFloat(), avgHumi))
                        nextAvg = true

                    }
                }
                if(!nextAvg){
                    // add last avg element
                    val avgTemp = currentAvgTemp.toFloat() / avgCounter
                    val avgHumi = currentAvgHumi.toFloat() / avgCounter
                    tempEntries.add(Entry(xLabel.toFloat(), avgTemp))
                    humiEntries.add(Entry(xLabel.toFloat(), avgHumi))
                }
                Collections.sort(tempEntries, EntryXComparator())
                Collections.sort(humiEntries, EntryXComparator())

                // x axis description
                val days = arrayOf("Son", "Mon", "Din", "Mit", "Don", "Fre", "Sam")
                val formatter: ValueFormatter = object : ValueFormatter() {
                    override fun getAxisLabel(value: Float, axis: AxisBase): String {
                        return if(dayBackMap.containsKey(value.toInt())) /*DayOfWeek.of(*/days[dayBackMap[value.toInt()]!!]/*)*/ else days[value.toInt()]
                    }
                }
                val xAxis: XAxis = chart.xAxis
                xAxis.valueFormatter = formatter
                xAxis.axisMaximum = 6f
                xAxis.axisMinimum = 0f
                val tempYAxis: YAxis = chart.axisLeft
                tempYAxis.axisMaximum = 20f
                tempYAxis.axisMinimum = 10f
                val humiYAxis: YAxis = chart.axisRight
                humiYAxis.axisMaximum = 55f
                humiYAxis.axisMinimum = 40f

                // define colors
                // light mode
                var tempColor = resources.getColor(R.color.champignon_5)
                val humiColor = resources.getColor(R.color.champignon_4)
                // dark mode
                val nightModeFlags = requireContext().resources.configuration.uiMode and
                        Configuration.UI_MODE_NIGHT_MASK
                if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                    tempColor = resources.getColor(R.color.champignon_stalk)
                    xAxis.textColor = Color.WHITE
                    tempYAxis.textColor = Color.WHITE
                    humiYAxis.textColor = Color.WHITE
                }

                // data sets
                val tempDataSet = LineDataSet(tempEntries, "Temperature")
                tempDataSet.axisDependency = YAxis.AxisDependency.LEFT
                tempDataSet.color = tempColor
                tempDataSet.valueTextColor = tempColor
                tempDataSet.setCircleColor(tempColor)
                val humiDataSet = LineDataSet(humiEntries, "Humidity")
                humiDataSet.axisDependency = YAxis.AxisDependency.RIGHT
                humiDataSet.color = humiColor
                humiDataSet.valueTextColor = humiColor
                humiDataSet.setCircleColor(humiColor)

                // combine datasets
                val dataSets: MutableList<ILineDataSet> = ArrayList()
                dataSets.add(tempDataSet)
                dataSets.add(humiDataSet)

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