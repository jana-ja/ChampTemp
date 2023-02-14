package de.janaja.champtemp.ui.main_content

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
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

        viewModel.tempHumis.observe(viewLifecycleOwner){tempHumiList ->
            if(tempHumiList.isNotEmpty()) {

                val chart = binding.chartWeek
                chart.description.text = "last 7 days"

                // entries
                val tempEntries = mutableListOf<Entry>()
                val humiEntries = mutableListOf<Entry>()
                // MPAndroid Chart needs the entries to be sorted by X axis.
                // X axis values are the days: mon -> 1, sun -> 7 (from java.time)
                // axis should not go from 1-7 everytime, but from oldest to newest day. map the values of the last 7 days to different values, so that they are "sorted"
                // (newest day (f.e. tue 2) -> 7, oldest day (f.e. wed 3) -> 1)

                val dayBackMap = HashMap<Int,Int>() // sorted day value -> actual day value
                var nextAvg = false
                // init first avg value
                var day: Int = tempHumiList[0].timestamp.dayOfWeek.value
                Log.e("Week", "day:  ${tempHumiList[0].timestamp.dayOfWeek} $day")
                var currentAvgTemp = tempHumiList[0].temp
                var currentAvgHumi = tempHumiList[0].humi
                var avgCounter = 0
                var i = 0
                var xLabel = 7
                dayBackMap[xLabel] = day
                for (tempHumi in tempHumiList.subList(1, tempHumiList.size)) {
                    if(nextAvg){
                        // begin of new avg
                        nextAvg = false
                        day = tempHumi.timestamp.dayOfWeek.value
                        Log.e("Week", "day: ${tempHumi.timestamp.dayOfWeek} $day")
                        currentAvgTemp = tempHumi.temp
                        currentAvgHumi = tempHumi.humi
                        avgCounter = 0
                        i++ // 0, 1, 2, 3, 4, 5, 6
                        xLabel = 7 - i // 7, 6, 5, 4, 3, 2, 1
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

                        if(tempEntries.size >= 7)
                            break

                        nextAvg = true
                    }
                }
                Collections.sort(tempEntries, EntryXComparator())
                Collections.sort(humiEntries, EntryXComparator())

                // x axis description: mon -> 1, son -> 7 (time.java)
                val days = arrayOf("Son", "Mon", "Din", "Mit", "Don", "Fre", "Sam", "Son")
                val formatter: ValueFormatter = object : ValueFormatter() {
                    override fun getAxisLabel(value: Float, axis: AxisBase): String {
                        Log.e("Week", value.toString())
                        return if(dayBackMap.containsKey(value.toInt())) /*DayOfWeek.of(*/days[dayBackMap[value.toInt()]!!]/*)*/ else days[value.toInt()]
                    }
                }
                val xAxis: XAxis = chart.xAxis
                xAxis.valueFormatter = formatter
                xAxis.axisMaximum = 7f
                xAxis.axisMinimum = 1f
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