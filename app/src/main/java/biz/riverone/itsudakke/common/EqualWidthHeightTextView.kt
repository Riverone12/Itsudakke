package biz.riverone.itsudakke.common

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView

/**
 * 縦横のサイズが同じテキスト
 * Created by kawahara on 2018/03/21.
 */

class EqualWidthHeightTextView : TextView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val r= Math.min(measuredWidth, measuredHeight)
        setMeasuredDimension(r, r)
    }
}