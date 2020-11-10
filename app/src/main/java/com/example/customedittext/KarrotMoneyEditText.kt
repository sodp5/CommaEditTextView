package com.example.customedittext

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatEditText
import java.lang.StringBuilder
import java.text.DecimalFormat

// 13글자 (소수점 위)
// 3글자 (소수점부터 소수점아래 2자리)
/**
 * Created by Dean on 2020/11/03
 */
class KarrotMoneyEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {
    init {
        setupTextInputType()
        setupParseMoneyString()
    }

    private fun setupTextInputType() {
        inputType = InputType.TYPE_CLASS_NUMBER
        keyListener = DigitsKeyListener.getInstance("0123456789.,")

        filters = arrayOf(InputFilter.LengthFilter(NUM_LENGTH + COMMA_LENGTH + DECIMAL_LENGTH + DOT_LENGTH))
    }

    private fun setupParseMoneyString() {
        val textWatcher = ParseMoneyStringTextWatcher()
        addTextChangedListener(textWatcher)
    }

    class ParseMoneyStringTextWatcher : TextWatcher {
        private var isEditing = false
        private var beforeNumber = ""
        private var beforeDecimal = ""
        private var changedText = ""

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            Log.d("beforeTextChanged", s.toString())

            s?.split(".")?.iterator()?.run {
                beforeNumber = next()
                if (hasNext()) {
                    beforeDecimal = next()
                }
            }
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            Log.d("onTextChanged", s.toString())

            val regexStr = s.toString().replace("[^\\d.]".toRegex(), "")
            if (regexStr.isEmpty()) {
                changedText = ""
                return
            }

            val hasDot = regexStr.contains(".")
            if (regexStr.count { it == '.' } > 1)
                return

            val splitsIterator = regexStr.split(".").iterator()

            val numberStr = splitsIterator.next()

            val numberBuilder = StringBuilder()

            val firstNum = when {
                numberStr.length > NUM_LENGTH -> {
                    beforeNumber
                }
                numberStr.isNotEmpty() -> {
                    val df = DecimalFormat("###,###")
                    df.format(numberStr.toLong())
                }
                else -> {
                    ""
                }
            }
            numberBuilder.append(firstNum)

            if (hasDot)
                numberBuilder.append(".")

            if (splitsIterator.hasNext()) {
                val decimalNum = splitsIterator.next()

                val realDecimalNum = if (decimalNum.length > DECIMAL_LENGTH) {
                    beforeDecimal
                } else {
                    decimalNum
                }
                numberBuilder.append(realDecimalNum)
            }

            changedText = numberBuilder.toString()
        }

        override fun afterTextChanged(s: Editable?) {
            Log.d("afterTextChanged", s.toString())
            if (isEditing) return
            isEditing = true

            s?.replace(0, s.length, changedText)

            isEditing = false
        }
    }

    companion object {
        const val NUM_LENGTH = 10
        const val COMMA_LENGTH = (NUM_LENGTH - 1) / 3
        const val DECIMAL_LENGTH = 2
        const val DOT_LENGTH = 1
    }
}