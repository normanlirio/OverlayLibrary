package com.example.imageoverlay.winsontanLibrary

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.winsontanlibrary.R
import java.util.ArrayList

class WScratchView : SurfaceView, IWScratchView, SurfaceHolder.Callback {

    // default value constants
    private val DEFAULT_COLOR = -0xbbbbbc // default color is dark gray
    private val DEFAULT_REVEAL_SIZE = 30

    private var mContext: Context? = null
    private var mThread: WScratchViewThread? = null
    internal var mPathList: MutableList<Path> = ArrayList()
    private var mOverlayColor: Int = 0
    private var mOverlayPaint: Paint? = null
    private var mRevealSize: Int = 0
    private var mIsScratchable = true
    private var mIsAntiAlias = false
    private var path: Path? = null
    private var startX = 0f
    private var startY = 0f
    private var mScratchStart = false
    private var mScratchBitmap: Bitmap? = null
    private var mScratchDrawable: Drawable? = null
    private var mBitmapPaint: Paint? = null
    private var mMatrix: Matrix? = null
    private var mScratchedTestBitmap: Bitmap? = null
    private var mScratchedTestCanvas: Canvas? = null
    private var mOnScratchCallback: OnScratchCallback? = null

    //Enable scratch all area if mClearCanvas is true
    private var mClearCanvas = false
    //Enable click on WScratchView if mIsClickable is true
    private var mIsClickable = false


    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs) {
        init(ctx, attrs)
    }

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        mContext = context

        // default value
        mOverlayColor = DEFAULT_COLOR
        mRevealSize = DEFAULT_REVEAL_SIZE

        val ta = context.obtainStyledAttributes(attrs, R.styleable.WScratchView, 0, 0)

        val indexCount = ta.indexCount
        for (i in 0 until indexCount) {
            val attr = ta.getIndex(i)
            when (attr) {
                R.styleable.WScratchView_overlayColor -> mOverlayColor = ta.getColor(attr, DEFAULT_COLOR)
                R.styleable.WScratchView_revealSize -> mRevealSize = ta.getDimensionPixelSize(attr, DEFAULT_REVEAL_SIZE)
                R.styleable.WScratchView_antiAlias -> mIsAntiAlias = ta.getBoolean(attr, false)
                R.styleable.WScratchView_scratchable -> mIsScratchable = ta.getBoolean(attr, true)
                R.styleable.WScratchView_scratchDrawable -> mScratchDrawable =
                    ta.getDrawable(R.styleable.WScratchView_scratchDrawable)
            }
        }

        setZOrderOnTop(true)
        val holder = holder
        holder.addCallback(this)
        holder.setFormat(PixelFormat.TRANSPARENT)

        mOverlayPaint = Paint()
        mOverlayPaint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        mOverlayPaint!!.style = Paint.Style.STROKE
        mOverlayPaint!!.strokeCap = Paint.Cap.ROUND
        mOverlayPaint!!.strokeJoin = Paint.Join.ROUND

        // convert drawable to bitmap if drawable already set in xml
        if (mScratchDrawable != null) {
            mScratchBitmap = (mScratchDrawable as BitmapDrawable).bitmap
        }

        mBitmapPaint = Paint()
        mBitmapPaint!!.isAntiAlias = true
        mBitmapPaint!!.isFilterBitmap = true
        mBitmapPaint!!.isDither = true
    }

    public override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //Clear all area if mClearCanvas is true
        if (mClearCanvas) {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            return
        }

        if (mScratchBitmap != null) {
            if (mMatrix == null) {
                val scaleWidth = canvas.width.toFloat() / mScratchBitmap!!.width
                val scaleHeight = canvas.height.toFloat() / mScratchBitmap!!.height
                mMatrix = Matrix()
                mMatrix!!.postScale(scaleWidth, scaleHeight)
            }
            canvas.drawBitmap(mScratchBitmap!!, mMatrix!!, mBitmapPaint)
        } else {
            canvas.drawColor(mOverlayColor)
        }

        for (path in mPathList) {
            mOverlayPaint!!.isAntiAlias = mIsAntiAlias
            mOverlayPaint!!.strokeWidth = mRevealSize.toFloat()

            canvas.drawPath(path, mOverlayPaint!!)
        }


    }

    private fun updateScratchedPercentage() {
        if (mOnScratchCallback == null) return
        mOnScratchCallback!!.onScratch(getScratchedRatio())
    }

    override fun onTouchEvent(me: MotionEvent): Boolean {
        synchronized(mThread!!.surfaceHolder) {
            if (!mIsScratchable) {
                return true
            }

            when (me.action) {
                MotionEvent.ACTION_DOWN -> {
                    path = Path()
                    path!!.moveTo(me.x, me.y)
                    startX = me.x
                    startY = me.y
                    mPathList.add(path!!)
                }
                MotionEvent.ACTION_MOVE -> {
                    if (mScratchStart) {
                        path!!.lineTo(me.x, me.y)
                    } else {
                        if (isScratch(startX, me.x, startY, me.y)) {
                            mScratchStart = true
                            path!!.lineTo(me.x, me.y)
                        }
                    }
                    updateScratchedPercentage()
                }
                MotionEvent.ACTION_UP -> {
                    //Set call back if user's finger detach
                    if (mOnScratchCallback != null) {
                        mOnScratchCallback!!.onDetach(true)
                    }
                    //perform Click action if the motion is not move
                    //and the WScratchView is clickable
                    if (!mScratchStart && mIsClickable) {
                        post { performClick() }
                    }
                    mScratchStart = false
                }
            }
            return true
        }
    }

    private fun isScratch(oldX: Float, x: Float, oldY: Float, y: Float): Boolean {
        val distance = Math.sqrt(Math.pow((oldX - x).toDouble(), 2.0) + Math.pow((oldY - y).toDouble(), 2.0)).toFloat()
        return distance > mRevealSize * 2
    }

    override fun surfaceChanged(arg0: SurfaceHolder, arg1: Int, arg2: Int, arg3: Int) {
        // do nothing
    }

    override fun surfaceCreated(arg0: SurfaceHolder) {
        mThread = WScratchViewThread(holder, this)
        mThread!!.setRunning(true)
        mThread!!.start()

        mScratchedTestBitmap =
            Bitmap.createBitmap(arg0.surfaceFrame.width(), arg0.surfaceFrame.height(), Bitmap.Config.ARGB_8888)
        mScratchedTestCanvas = Canvas(mScratchedTestBitmap!!)
    }

    override fun surfaceDestroyed(arg0: SurfaceHolder) {
        var retry = true
        mThread!!.setRunning(false)
        while (retry) {
            try {
                mThread!!.join()
                retry = false
            } catch (e: InterruptedException) {
                // do nothing but keep retry
            }

        }

    }

    internal inner class WScratchViewThread(val surfaceHolder: SurfaceHolder, private val mView: WScratchView) :
        Thread() {
        private var mRun = false

        fun setRunning(run: Boolean) {
            mRun = run
        }

        override fun run() {
            var c: Canvas?
            while (mRun) {
                c = null
                try {
                    c = surfaceHolder.lockCanvas(null)
                    synchronized(surfaceHolder) {
                        if (c != null) {
                            mView.draw(c)
                        }
                    }
                } finally {
                    if (c != null) {
                        surfaceHolder.unlockCanvasAndPost(c)
                    }
                }
            }
        }
    }

    override fun isScratchable(): Boolean {
        return mIsScratchable
    }

    override fun setScratchable(flag: Boolean) {
         mIsScratchable = flag
    }

    override fun setOnScratchCallback(callback: OnScratchCallback) {
        mOnScratchCallback = callback
    }

    override fun getScratchedRatio(): Float {
        return getScratchedRatio(DEFAULT_SCRATCH_TEST_SPEED)
    }


    override fun resetView() {
        synchronized(mThread!!.surfaceHolder) {
            mPathList.clear()
        }
    }

    override fun setOverlayColor(ResId: Int) {
        mOverlayColor = ResId
    }

    override fun setRevealSize(size: Int) {
        mRevealSize = size
    }

    override fun setAntiAlias(flag: Boolean) {
        mIsAntiAlias = flag
    }

    override fun setScratchDrawable(d: Drawable) {
        mScratchDrawable = d
        if (mScratchDrawable != null) {
            mScratchBitmap = (mScratchDrawable as BitmapDrawable).bitmap
        }
    }

    override fun setScratchBitmap(b: Bitmap) {
        mScratchBitmap = b
    }

    /**
     * thanks to https://github.com/daveyfong for providing this method
     */
    override fun getScratchedRatio(speed: Int): Float {
        if (null == mScratchedTestBitmap) {
            return 0f
        }
        draw(mScratchedTestCanvas)

        val width = mScratchedTestBitmap!!.width
        val height = mScratchedTestBitmap!!.height

        var count = 0
        var i = 0
        while (i < width) {
            var j = 0
            while (j < height) {
                if (0 == Color.alpha(mScratchedTestBitmap!!.getPixel(i, j))) {
                    count++
                }
                j += speed
            }
            i += speed
        }

        return count.toFloat() / (width / speed * (height / speed)) * 100
    }


    abstract class OnScratchCallback {
        abstract fun onScratch(percentage: Float)
        //Call back funtion to monitor the status of finger
        abstract fun onDetach(fingerDetach: Boolean)
    }

    //Set the mClearCanvas
    override fun setScratchAll(scratchAll: Boolean) {
        mClearCanvas = scratchAll
    }

    //Set the WScartchView clickable
    override fun setBackgroundClickable(clickable: Boolean) {
        mIsClickable = clickable
    }

    companion object {
        private val TAG = "WScratchView"

        val DEFAULT_SCRATCH_TEST_SPEED = 4
    }
}
