package com.example.imageoverlay

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.imageoverlay.winsontanLibrary.WScratchView
import com.example.winsontanlibrary.R

class DefaultCodes : Activity() {
    private var scratchView: WScratchView? = null
    private var percentageView: TextView? = null
    private var mPercentage: Float = 0.toFloat()

    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.default_codes)

        percentageView = findViewById<View>(R.id.percentage) as TextView
        scratchView = findViewById<View>(R.id.scratch_view) as WScratchView

        // customize attribute programmatically
        scratchView!!.setScratchable(true)
        scratchView!!.setRevealSize(50)
        scratchView!!.setAntiAlias(true)
        scratchView!!.setOverlayColor(Color.RED)
        scratchView!!.setBackgroundClickable(true)

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

        scratchView!!.setOnClickListener(View.OnClickListener {
            Toast.makeText(
                this@DefaultCodes,
                "Clicked",
                Toast.LENGTH_SHORT
            ).show()
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
        getMenuInflater().inflate(R.menu.activity_main, menu)
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