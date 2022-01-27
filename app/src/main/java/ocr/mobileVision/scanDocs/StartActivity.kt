package ocr.mobileVision.scanDocs
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button



class StartActivity : AppCompatActivity() {


    private lateinit var buttonIdCard: Button
    private lateinit var buttonSejour: Button
    private lateinit var buttonPassport: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.start)
       buttonIdCard = findViewById(R.id.buttonIdCard)
        buttonSejour = findViewById(R.id.buttonSejour)
        buttonPassport = findViewById(R.id.buttonPassport)
        buttonIdCard.setOnClickListener {
            val intent = Intent(this, IDCard::class.java)
            startActivity(intent)
        }
        buttonPassport.setOnClickListener {
            val intent = Intent(this, ScanFrontPassport::class.java)
            startActivity(intent)
        }
        buttonSejour.setOnClickListener {
            val intent = Intent(this, ScanFrontSejour::class.java)
            startActivity(intent)
        }
    }

}