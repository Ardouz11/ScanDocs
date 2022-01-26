package ocr.mobileVision.scanDocs
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView


class DataExtracted : AppCompatActivity() {


    private lateinit var tv_result: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.data_extracted)
        tv_result = findViewById(R.id.tv_result)
        val extras = intent.extras
        if (extras != null) {
            val value = extras.getString("dataCIN")
            tv_result.setText(value)
        }

    }

}