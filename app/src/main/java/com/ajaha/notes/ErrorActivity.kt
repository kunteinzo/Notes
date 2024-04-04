package com.ajaha.notes

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


/**
 * From Sketchware DebugActivity
 * Credit to Sketchware
 * */
class ErrorActivity : AppCompatActivity() {

    private lateinit var errorText: TextView

    private val exceptionTypes = arrayOf(
        "StringIndexOutOfBoundsException",
        "IndexOutOfBoundsException",
        "ArithmeticException",
        "NumberFormatException",
        "ActivityNotFoundException"
    )

    private val exceptionMessages = arrayOf(
        "Invalid string operation\n",
        "Invalid list operation\n",
        "Invalid arithmetical operation\n",
        "Invalid toNumber block operation\n",
        "Invalid intent operation"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_error)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        errorText = findViewById(R.id.error_text)

        val intent = intent
        var errorMessage: String? = ""
        var madeErrorMessage: String? = ""

        if (intent != null) {
            errorMessage = intent.getStringExtra("error")
            val split = errorMessage!!.split("\n".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            //errorMessage = split[0];
            try {
                for (j in exceptionTypes.indices) {
                    if (split[0].contains(exceptionTypes[j])) {
                        madeErrorMessage = exceptionMessages[j]
                        val addIndex =
                            split[0].indexOf(exceptionTypes[j]) + exceptionTypes[j].length
                        madeErrorMessage += split[0].substring(addIndex, split[0].length)
                        madeErrorMessage += "\n\nDetailed error message:\n$errorMessage"
                        break
                    }
                }
                if (madeErrorMessage!!.isEmpty()) {
                    madeErrorMessage = errorMessage
                }
            } catch (e: Exception) {
                errorMessage = """
            $madeErrorMessage
            
            Error while getting error: ${Log.getStackTraceString(e)}
            """.trimIndent()
            }
        }

        errorText.text = errorMessage
    }
}