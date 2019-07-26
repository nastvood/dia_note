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
import android.widget.*
import com.androidplot.xy.*
import java.lang.Exception
import java.text.FieldPosition
import java.text.Format
import java.text.ParsePosition
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneOffset

private const val SETTING_SHOWED_TYPE = "showed_type"
private const val SETTING_SHOWED_TYPE_DEFAULT = 0x2

class CalendarFragment : Fragment() {
    private var listener: OnFragmentInteractionListener? = null
    lateinit var plot:XYPlot
    lateinit var  dp:DatePicker
    private lateinit var db:AppDatabase
    lateinit var sb:SeekBar
    lateinit var tvSbValue:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = AppDatabase.getInstance(activity!!.applicationContext)!!
    }

    fun showedType():Int {
        val settings = this.context!!.getSharedPreferences(resources.getString(R.string.app_name), Context.MODE_PRIVATE)
        return settings.getInt(SETTING_SHOWED_TYPE, SETTING_SHOWED_TYPE_DEFAULT)
    }

    fun updateShowedType(newShowedType:Int) {
        val settings =  this.context!!.getSharedPreferences(resources.getString(R.string.app_name), Context.MODE_PRIVATE)
        settings.edit()!!.apply {
            putInt(SETTING_SHOWED_TYPE, newShowedType)
            apply()
        }
    }

    private fun dataPlot() {
        plot.clear()

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

        val seriesRapid = SimpleXYSeries(valsRapid, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, NoteType.RAPID.name)
        val seriesLong = SimpleXYSeries(valsLong, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, NoteType.LONG.name)

        val seriesRapidFormatter = LineAndPointFormatter().apply {
            linePaint.strokeWidth = 5f
            linePaint.color = Color.argb(255,0,0xAA, 0)
            vertexPaint.color = Color.argb(255,0,0x77, 0)
            vertexPaint.strokeWidth = 20f
            fillPaint.color = 0
            pointLabelFormatter.textPaint.color = Color.argb(255,0xCC,0xCC, 0xCC)
            interpolationParams = CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal)
        }

        val seriesLongFormatter = LineAndPointFormatter().apply {
            linePaint.strokeWidth = 5f
            linePaint.color = Color.argb(255,0, 0,0xAA)
            vertexPaint.color = Color.argb(255,0, 0,0x99)
            vertexPaint.strokeWidth = 20f
            fillPaint.color = 0
            pointLabelFormatter.textPaint.color = Color.argb(255,0xCC,0xCC, 0xCC)
            interpolationParams = CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal)
        }

        Log.v("calendar", "%f %d".format(seriesLongFormatter.vertexPaint.strokeWidth, showedType().toByte()))

        when (showedType().toByte()) {
            NoteType.RAPID.value -> plot.addSeries(seriesRapid, seriesRapidFormatter)
            NoteType.LONG.value -> plot.addSeries(seriesLong, seriesLongFormatter)
            else -> {
                plot.addSeries(seriesRapid, seriesRapidFormatter)
                plot.addSeries(seriesLong, seriesLongFormatter)
            }
        }

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

        plot.redraw()
    }

    fun showSeekBarText(noteTypeValue:Int) {
        when (noteTypeValue.toByte()) {
            NoteType.RAPID.value -> tvSbValue.setText(R.string.rapid_type)
            NoteType.LONG.value -> tvSbValue.setText(R.string.long_type)
            else -> tvSbValue.setText(R.string.long_rapid_type)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        dp = view.findViewById(R.id.dp_calendar_notes)
        dp.setOnDateChangedListener { _, _, _, _ ->
            dataPlot()
        }

        plot = view.findViewById(R.id.plot_calendar_notes)
        val ll:LinearLayout = view.findViewById(R.id.ll_calendar_notes)
        tvSbValue = view.findViewById(R.id.tv_sb_calendar_notes)

        try {
            val resId = Resources.getSystem().getIdentifier("day", "id", "android")
            dp.findViewById<View>(resId).visibility = View.GONE
        } catch (e:Exception) {}

        val btnSettings:Button = view.findViewById(R.id.btn_calendar_notes)
        btnSettings.setOnClickListener {
            if (ll.visibility == View.VISIBLE) {
                ll.visibility = View.GONE
            } else {
                ll.visibility = View.VISIBLE
            }

        }

        sb = view.findViewById(R.id.sb_calendar_notes)
        sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p1: Int, p2: Boolean) {
                showSeekBarText(sb!!.progress)
                updateShowedType(sb.progress)

                dataPlot()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })

        sb.progress = showedType()
        showSeekBarText(showedType())

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
