package com.nastvood.dianote

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.room.Room
import com.androidplot.xy.*
import kotlinx.android.synthetic.main.fragment_note_edit.*
import java.lang.Exception
import java.text.FieldPosition
import java.text.Format
import java.text.ParsePosition
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneOffset
import java.util.*

class CalendarFragment : Fragment() {
    private var listener: OnFragmentInteractionListener? = null
    lateinit var plot:XYPlot
    lateinit var  dp:DatePicker
    private lateinit var db:AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Room.databaseBuilder(activity!!.applicationContext, AppDatabase::class.java, resources.getString(R.string.app_name))
            .allowMainThreadQueries()
            .build()
    }

    private fun dataPlot() {
        val startDate = LocalDateTime.of(dp.year, dp.month + 1, 1, 0, 0, 0)
        val start = startDate.toInstant(ZoneOffset.UTC).toEpochMilli()
        val lengthOfMonth = YearMonth.of(dp.year, dp.month + 1).lengthOfMonth()

        val finish = LocalDateTime.of(dp.year, dp.month + 1, lengthOfMonth, 23, 59, 59)
            .toInstant(ZoneOffset.UTC).toEpochMilli()

        val data = db.noteDao().groupNotesByDate(start, finish)


        val days = (1..lengthOfMonth)
        val valsRapid = days.map { 0 }.toMutableList()
        val valsLong = days.map { 0 }.toMutableList()
        for (v in data) {
            when (v.type) {
                NoteType.RAPID -> valsRapid[v.day - 1] = v.sum
                NoteType.LONG -> valsLong[v.day - 1] = v.sum
            }
        }

        //val labels = SimpleXYSeries(days.toMutableList(), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Days")

        val seriesRapid = SimpleXYSeries(valsRapid, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, NoteType.RAPID.name)
        val seriesLong = SimpleXYSeries(valsLong, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, NoteType.LONG.name)

        val seriesRapidFormatter = LineAndPointFormatter()
        seriesRapidFormatter.apply {
            linePaint.strokeWidth = 5f
            linePaint.color = Color.argb(255,0,0xAA, 0)
            vertexPaint.color = Color.argb(255,0,0x77, 0)
            vertexPaint.strokeWidth = 20f
            fillPaint.color = 0
            pointLabelFormatter.textPaint.color = Color.argb(255,0xCC,0xCC, 0xCC)
            interpolationParams = CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal)
        }

        val seriesLongFormatter = LineAndPointFormatter()
        seriesLongFormatter.apply {
            linePaint.strokeWidth = 5f
            linePaint.color = Color.argb(255,0, 0,0xAA)
            vertexPaint.color = Color.argb(255,0, 0,0x99)
            vertexPaint.strokeWidth = 20f
            fillPaint.color = 0
            pointLabelFormatter.textPaint.color = Color.argb(255,0xCC,0xCC, 0xCC)
            interpolationParams = CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal)
        }

        Log.v("calendar", "%f".format(seriesLongFormatter.vertexPaint.strokeWidth))

        plot.addSeries(seriesRapid, seriesRapidFormatter)
        plot.addSeries(seriesLong, seriesLongFormatter)

        plot.graph.getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).format = object:Format() {
            override fun format(obj:Any, toAppendTo:StringBuffer, pos:FieldPosition):StringBuffer {
                //Log.v ("convert", "%s".format(obj.toString()))
                val i = Math.round((obj as Number).toFloat())
                return toAppendTo.append(i)
            }

            override fun parseObject(source:String, pos:ParsePosition):Any? {
                return null
            }
        }

        for (v in days) {
            //Log.v("calendar", "%s %d %d %d %d".format(v.type.name, v.day, v.month, v.year, v.sum))
           // Log.v("calendar", "%d %d %d".format(v, valsLong[v - 1], valsRapid[v - 1]))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        dp = view.findViewById(R.id.dp_calendar_notes)
        plot = view.findViewById(R.id.plot_calendar_notes)

        try {
            val resId = Resources.getSystem().getIdentifier("day", "id", "android")
            dp.findViewById<View>(resId).visibility = View.GONE
        } catch (e:Exception) {}

        dataPlot()

        return view
    }

    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            CalendarFragment().apply {
            }
    }
}
