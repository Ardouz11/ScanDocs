package ocr.mobileVision.scanDocs
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DataExtracted : AppCompatActivity() {

    private lateinit var tvResult: TextView
    private lateinit var buttonBack: TextView
    private lateinit var prenomCinResult: TextView
    private lateinit var nomCinResult: TextView
    private lateinit var dateCinResult: TextView
    private lateinit var numberCinResult: TextView
    private lateinit var adresseCinResult: TextView
    private lateinit var sexeCinResult: TextView
    private lateinit var nationaliteSejourResult: TextView
    private lateinit var validiteSejourResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.data_extracted)

        tvResult = findViewById(R.id.tv_result)
        buttonBack = findViewById(R.id.back_Btn)
        prenomCinResult = findViewById(R.id.prenom_cin_result)
        nomCinResult = findViewById(R.id.nom_cin_result)
        dateCinResult = findViewById(R.id.date_cin_result)
        numberCinResult = findViewById(R.id.number_cin_result)
        adresseCinResult = findViewById(R.id.adresse_cin_result)
        sexeCinResult = findViewById(R.id.sexe_cin_result)
        nationaliteSejourResult = findViewById(R.id.nationalite_sejour_result)
        validiteSejourResult = findViewById(R.id.validite_sejour_result)

        val hashMap: HashMap<String, String>
        val fromActivityValue: String
        val extras = intent.extras

        if (extras != null) {
            val intent = getIntent()
            fromActivityValue = intent.getSerializableExtra("fromActivity") as String
            hashMap = intent.getSerializableExtra("dataCIN") as HashMap<String, String>

            // we verify the last activity
            when (fromActivityValue) {
                "cin" -> setCinView(hashMap)
                "sejour" -> setSejourView(hashMap)
                "passport" -> setCinView(hashMap)
                "sim" -> setCinView(hashMap)
            }
            for ((key, value) in hashMap) {
                Log.d("TestPlot : ", "- $key" + " : $value")
                tvResult.text = tvResult.text.toString() + key + " : " + value + "\n"
            }
        }

        buttonBack.setOnClickListener {
            val intent = Intent(this, StartActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Function to set the value
     */
    private fun setCinView(result: HashMap<String, String>) {
        showTextCin()
        prenomCinResult.text = result["Prenom"].toString()
        nomCinResult.text = result["Nom"].toString()
        dateCinResult.text = result["DOB"].toString()
        numberCinResult.text = result["CIN"].toString()
        adresseCinResult.text = result["Adresse"].toString()
        sexeCinResult.text = result["Sexe"].toString()
    }

    /**
     * Function to show cin result part
     */
    private fun showTextCin() {
        prenomCinResult.visibility = View.VISIBLE
        nomCinResult.visibility = View.VISIBLE
        dateCinResult.visibility = View.VISIBLE
        numberCinResult.visibility = View.VISIBLE
        adresseCinResult.visibility = View.VISIBLE
        sexeCinResult.visibility = View.VISIBLE
    }

    /**
     *  Function to set the value of sejour result
     */
    private fun setSejourView(result: HashMap<String, String>) {
        showTextSejour()
        prenomCinResult.text = result["Prenom"].toString()
        nomCinResult.text = result["Nom"].toString()
        dateCinResult.text = result["DOB"].toString()
        numberCinResult.text = result["CIN"].toString()
        adresseCinResult.text = result["Adresse"].toString()
        sexeCinResult.text = result["Sexe"].toString()
        // nationaliteSejourResult.text = result[""].toString()
        // validiteSejourResult.text = result[""].toString()
    }

    /**
     * Function to show sejour result part
     */
    private fun showTextSejour() {
        showTextCin() // same field like cin part
        nationaliteSejourResult.visibility = View.VISIBLE
        sexeCinResult.visibility = View.VISIBLE
    }
}
