package ocr.mobileVision.scanDocs
import android.content.Intent
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

class StartActivity : AppCompatActivity() {

    var methodSelected: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.start)

        val containerCin = findViewById<ConstraintLayout>(R.id.container_cin)
        val checkCin = findViewById<ImageView>(R.id.check_cin)

        val containerPassport = findViewById<ConstraintLayout>(R.id.container_passport)
        val checkPassport = findViewById<ImageView>(R.id.check_passport)

        val containerSejour = findViewById<ConstraintLayout>(R.id.container_sejour)
        val checkSejour = findViewById<ImageView>(R.id.check_sejour)

        val nextBtn = findViewById<TextView>(R.id.next_Btn)

        containerCin.setOnClickListener {
            methodSelected = "cin"
            checkCin.setImageResource(R.drawable.selected_radio)
            containerCin.setBackgroundResource(R.drawable.border_identity_violet)

            /**
             * Unselect Other
             */
            checkPassport.setImageResource(R.drawable.unselected_radio)
            containerPassport.setBackgroundResource(R.drawable.our_item_background)
            checkSejour.setImageResource(R.drawable.unselected_radio)
            containerSejour.setBackgroundResource(R.drawable.our_item_background)

            nextBtn.background = ContextCompat.getDrawable(this, R.drawable.bg_button_confim)
        }

        containerPassport.setOnClickListener {
            methodSelected = "passport"
            checkPassport.setImageResource(R.drawable.selected_radio)
            containerPassport.setBackgroundResource(R.drawable.border_identity_violet)

            /**
             * Unselect Other
             */
            checkCin.setImageResource(R.drawable.unselected_radio)
            containerCin.setBackgroundResource(R.drawable.our_item_background)
            checkSejour.setImageResource(R.drawable.unselected_radio)
            containerSejour.setBackgroundResource(R.drawable.our_item_background)

            nextBtn.background = ContextCompat.getDrawable(this, R.drawable.bg_button_confim)
        }

        containerSejour.setOnClickListener {
            methodSelected = "sejour"
            checkSejour.setImageResource(R.drawable.selected_radio)
            containerSejour.setBackgroundResource(R.drawable.border_identity_violet)

            /**
             * Unselect Other
             */
            checkPassport.setImageResource(R.drawable.unselected_radio)
            containerPassport.setBackgroundResource(R.drawable.our_item_background)
            checkCin.setImageResource(R.drawable.unselected_radio)
            containerCin.setBackgroundResource(R.drawable.our_item_background)

            nextBtn.background = ContextCompat.getDrawable(this, R.drawable.bg_button_confim)
        }

        nextBtn.setOnClickListener {
            if (methodSelected == "passport") {
                val intentScanFrontPassport = Intent(this, ScanFrontPassport::class.java)
                startActivity(intentScanFrontPassport)
            } else if (methodSelected == "sejour") {
                val intentScanFrontSejour = Intent(this, ScanFrontSejour::class.java)
                startActivity(intentScanFrontSejour)
            } else {
                val intentIDCard = Intent(this, IDCard::class.java)
                startActivity(intentIDCard)
            }
        }
    }
}
