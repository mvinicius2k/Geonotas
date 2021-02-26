package br.ufc.geonotas.myPalette

import android.content.Context
import android.util.AttributeSet

class Icon : androidx.appcompat.widget.AppCompatImageView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, defStyleAttr: Int ,attrs: AttributeSet?) : super(context, attrs, defStyleAttr)


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width =  measuredWidth
        val height = measuredHeight
        if(width > height)
            setMeasuredDimension(height, height)
        else
            setMeasuredDimension(width, width)
    }

}