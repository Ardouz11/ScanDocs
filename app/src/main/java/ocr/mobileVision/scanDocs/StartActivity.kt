package ocr.mobileVision.scanDocs
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat

class StartActivity : AppCompatActivity() {

    private var methodSelected: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.start)

        val containerCin = findViewById<ConstraintLayout>(R.id.container_cin)
        val checkCin = findViewById<ImageView>(R.id.check_cin)

        val containerPassport = findViewById<ConstraintLayout>(R.id.container_passport)
        val checkPassport = findViewById<ImageView>(R.id.check_passport)

        val containerSejour = findViewById<ConstraintLayout>(R.id.container_sejour)
        val checkSejour = findViewById<ImageView>(R.id.check_sejour)
        val containerSim = findViewById<ConstraintLayout>(R.id.container_sim)
        val checkSim = findViewById<ImageView>(R.id.check_sim)

        val viewCin = findViewById<View>(R.id.view_unselected_cin)
        val viewPassport = findViewById<View>(R.id.view_unselected_passport)
        val viewSejour = findViewById<View>(R.id.view_unselected_sejour)
        val viewSim = findViewById<View>(R.id.view_unselected_sim)

        val nextBtn = findViewById<TextView>(R.id.next_Btn)

        nextBtn.setEnabled(false)

        containerCin.setOnClickListener {
            nextBtn.setEnabled(true)
            methodSelected = "cin"
            checkCin.setImageResource(R.drawable.selected_radio)
            containerCin.setBackgroundResource(R.drawable.border_identity_violet)
            viewCin.visibility = View.GONE

            /**
             * Unselect Other
             */
            viewSim.visibility = View.VISIBLE
            viewSejour.visibility = View.VISIBLE
            viewPassport.visibility = View.VISIBLE
            checkPassport.setImageResource(R.drawable.unselected_radio)
            containerPassport.setBackgroundResource(R.drawable.border_identity_white)
            checkSejour.setImageResource(R.drawable.unselected_radio)
            containerSejour.setBackgroundResource(R.drawable.border_identity_white)
            checkSim.setImageResource(R.drawable.unselected_radio)
            containerSim.setBackgroundResource(R.drawable.border_identity_white)
            nextBtn.background = ContextCompat.getDrawable(this, R.drawable.bg_button_confim)
        }

        containerPassport.setOnClickListener {
            nextBtn.setEnabled(true)
            methodSelected = "passport"
            checkPassport.setImageResource(R.drawable.selected_radio)
            containerPassport.setBackgroundResource(R.drawable.border_identity_violet)
            viewPassport.visibility = View.GONE

            /**
             * Unselect Other
             */
            viewSim.visibility = View.VISIBLE
            viewSejour.visibility = View.VISIBLE
            viewCin.visibility = View.VISIBLE
            checkCin.setImageResource(R.drawable.unselected_radio)
            containerCin.setBackgroundResource(R.drawable.border_identity_white)
            checkSejour.setImageResource(R.drawable.unselected_radio)
            containerSejour.setBackgroundResource(R.drawable.border_identity_white)
            checkSim.setImageResource(R.drawable.unselected_radio)
            containerSim.setBackgroundResource(R.drawable.border_identity_white)
            nextBtn.background = ContextCompat.getDrawable(this, R.drawable.bg_button_confim)
        }

        containerSejour.setOnClickListener {
            nextBtn.setEnabled(true)
            methodSelected = "sejour"
            checkSejour.setImageResource(R.drawable.selected_radio)
            containerSejour.setBackgroundResource(R.drawable.border_identity_violet)
            viewSejour.visibility = View.GONE

            /**
             * Unselect Other
             */
            viewSim.visibility = View.VISIBLE
            viewPassport.visibility = View.VISIBLE
            viewCin.visibility = View.VISIBLE
            checkPassport.setImageResource(R.drawable.unselected_radio)
            containerPassport.setBackgroundResource(R.drawable.border_identity_white)
            checkCin.setImageResource(R.drawable.unselected_radio)
            containerCin.setBackgroundResource(R.drawable.border_identity_white)
            checkSim.setImageResource(R.drawable.unselected_radio)
            containerSim.setBackgroundResource(R.drawable.border_identity_white)
            nextBtn.background = ContextCompat.getDrawable(this, R.drawable.bg_button_confim)
        }
        containerSim.setOnClickListener {
            nextBtn.setEnabled(true)
            methodSelected = "sim"
            checkSim.setImageResource(R.drawable.selected_radio)
            containerSim.setBackgroundResource(R.drawable.border_identity_violet)
            viewSim.visibility = View.GONE

            /**
             * Unselect Other
             */
            viewSejour.visibility = View.VISIBLE
            viewPassport.visibility = View.VISIBLE
            viewCin.visibility = View.VISIBLE
            checkPassport.setImageResource(R.drawable.unselected_radio)
            containerPassport.setBackgroundResource(R.drawable.border_identity_white)
            checkCin.setImageResource(R.drawable.unselected_radio)
            containerCin.setBackgroundResource(R.drawable.border_identity_white)
            checkSejour.setImageResource(R.drawable.unselected_radio)
            containerSejour.setBackgroundResource(R.drawable.border_identity_white)

            nextBtn.background = ContextCompat.getDrawable(this, R.drawable.bg_button_confim)
        }

        nextBtn.setOnClickListener {
            when (methodSelected) {
                "passport" -> {
                    val intentScanFrontPassport = Intent(this, ScanFrontPassport::class.java)
                    startActivity(intentScanFrontPassport)
                }
                "sejour" -> {
                    val intentScanFrontSejour = Intent(this, ScanFrontSejour::class.java)
                    startActivity(intentScanFrontSejour)
                }
                "sim" -> {
                    val intentScanSim = Intent(this, SimScan::class.java)
                    startActivity(intentScanSim)
                }
                else -> {
                    val intentIDCard = Intent(this, IDCard::class.java)
                    startActivity(intentIDCard)
                }
            }
        }
    }
}
