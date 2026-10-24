package com.example.calculator

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.calculator.databinding.ActivityMainBinding
import java.math.BigDecimal
import java.math.RoundingMode

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var current = StringBuilder("0")
    private var previous: BigDecimal? = null
    private var pendingOp: Char? = null
    private var justEvaluated = false
    private var hasDot = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val digitIds = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        )
        digitIds.forEachIndexed { digit, id ->
            findViewById<Button>(id).setOnClickListener { onDigit(digit) }
        }

        binding.btnDot.setOnClickListener { onDot() }
        binding.btnC.setOnClickListener { onClear() }
        binding.btnSign.setOnClickListener { onSign() }
        binding.btnPercent.setOnClickListener { onPercent() }
        binding.btnAdd.setOnClickListener { onOperator('+') }
        binding.btnSubtract.setOnClickListener { onOperator('-') }
        binding.btnMultiply.setOnClickListener { onOperator('*') }
        binding.btnDivide.setOnClickListener { onOperator('/') }
        binding.btnEquals.setOnClickListener { onEquals() }

        updateDisplay()
    }

    private fun onDigit(d: Int) {
        if (justEvaluated) {
            current = StringBuilder()
            justEvaluated = false
            hasDot = false
        }
        if (current.toString() == "0" && d != 0 && !hasDot) {
            current = StringBuilder()
        }
        current.append(d)
        updateDisplay()
    }

    private fun onDot() {
        if (justEvaluated) {
            current = StringBuilder("0")
            justEvaluated = false
            hasDot = false
        }
        if (!hasDot) {
            if (current.isEmpty()) current.append("0")
            current.append(".")
            hasDot = true
            updateDisplay()
        }
    }

    private fun onClear() {
        current = StringBuilder("0")
        previous = null
        pendingOp = null
        justEvaluated = false
        hasDot = false
        updateDisplay()
    }

    private fun onSign() {
        if (current.toString() == "0") return
        if (current.startsWith("-")) current.deleteCharAt(0) else current.insert(0, "-")
        updateDisplay()
    }

    private fun onPercent() {
        val v = parseCurrent()
        val r = v.divide(BigDecimal(100), 10, RoundingMode.HALF_UP).stripTrailingZeros()
        current = StringBuilder(format(r))
        hasDot = current.contains(".")
        justEvaluated = false
        updateDisplay()
    }

    private fun onOperator(op: Char) {
        if (pendingOp != null && !justEvaluated) {
            evaluate()
        }
        previous = parseCurrent()
        pendingOp = op
        justEvaluated = true
        hasDot = false
    }

    private fun onEquals() {
        if (pendingOp == null) return
        evaluate()
        pendingOp = null
        justEvaluated = true
        hasDot = current.contains(".")
    }

    private fun evaluate() {
        val a = previous ?: return
        val b = parseCurrent()
        val result = when (pendingOp) {
            '+' -> a.add(b)
            '-' -> a.subtract(b)
            '*' -> a.multiply(b)
            '/' -> {
                if (b.compareTo(BigDecimal.ZERO) == 0) {
                    showError()
                    return
                }
                a.divide(b, 10, RoundingMode.HALF_UP)
            }
            else -> return
        }
        val formatted = format(result.stripTrailingZeros())
        current = StringBuilder(formatted)
        previous = null
        hasDot = formatted.contains(".")
        updateDisplay()
    }

    private fun showError() {
        current = StringBuilder("Error")
        previous = null
        pendingOp = null
        justEvaluated = true
        hasDot = false
        updateDisplay()
    }

    private fun parseCurrent(): BigDecimal {
        val s = current.toString()
        return if (s == "Error" || s.isBlank()) BigDecimal.ZERO else BigDecimal(s)
    }

    private fun format(v: BigDecimal): String {
        val plain = v.toPlainString()
        return if (plain == "0" || plain == "-0") "0" else plain
    }

    private fun updateDisplay() {
        binding.display.text = current.toString()
    }
}
