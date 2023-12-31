package com.example.mystorysubmission.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.example.mystorysubmission.R

class LoginButton : AppCompatButton {
    private lateinit var validBackground: Drawable
    private lateinit var invalidBackground: Drawable

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        validBackground = ContextCompat.getDrawable(context, R.drawable.bg_valid_login) as Drawable
        invalidBackground =
            ContextCompat.getDrawable(context, R.drawable.bg_invalid_login) as Drawable

        background = invalidBackground
        isEnabled = false
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

    fun changeStatus(status: Boolean) {
        background = if (status) validBackground else invalidBackground
        isEnabled = status
    }
}