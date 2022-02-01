package ocr.mobileVision.scanDocs
import android.content.Intent
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView


class DataExtracted : AppCompatActivity() {


    private lateinit var tvResult: TextView
    private lateinit var buttonBack: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.data_extracted)
        tvResult = findViewById(R.id.tv_result)
        buttonBack=findViewById(R.id.back_Btn)

        val extras = intent.extras

        if (extras != null) {
            val value = extras.getString("dataCIN")
            tvResult.text = value
        }

        buttonBack.setOnClickListener {
            val intent = Intent(this, StartActivity::class.java)
            startActivity(intent)
        }

    }

}