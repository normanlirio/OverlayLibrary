package com.example.imageoverlay

import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.TextView
import com.example.imageoverlay.winsontanLibrary.WScratchView

class DefaultXML : Activity() {
    private var scratchView: WScratchView? = null
    private var percentageView: TextView? = null
    private var mPercentage: Float = 0.toFloat()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.default_xml)

        percentageView = findViewById<View>(R.id.percentage) as TextView

        scratchView = findViewById<View>(R.id.scratch_view) as WScratchView

        // add callback for update scratch percentage
        scratchView!!.setOnScratchCallback(object : WScratchView.OnScratchCallback() {

            override fun onScratch(percentage: Float) {
                updatePercentage(percentage)
            }

            override fun onDetach(fingerDetach: Boolean) {
                if (mPercentage > 50) {
                    scratchView!!.setScratchAll(true)
                    updatePercentage(100f)
                }
            }
        })

        updatePercentage(0f)
    }

    protected fun updatePercentage(percentage: Float) {
        mPercentage = percentage
        val percentage2decimal = String.format("%.2f", percentage) + " %"
        percentageView!!.text = percentage2decimal
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.activity_main, menu)
        return true
    }

    fun onClickHandler(view: View) {
        when (view.id) {
            R.id.reset_button -> {
                scratchView!!.resetView()
                scratchView!!.setScratchAll(false) // todo: should include to resetView?
                updatePercentage(0f)
            }
        }
    }
}
