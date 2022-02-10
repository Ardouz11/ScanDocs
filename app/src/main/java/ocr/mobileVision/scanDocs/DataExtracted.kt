package ocr.mobileVision.scanDocs
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DataExtracted : AppCompatActivity() {

    private lateinit var tvResult: TextView
    private lateinit var buttonBack: TextView
    // CIN
    private lateinit var prenomCinResult: TextView
    private lateinit var nomCinResult: TextView
    private lateinit var dateCinResult: TextView
    private lateinit var numberCinResult: TextView
    private lateinit var adresseCinResult: TextView
    private lateinit var sexeCinResult: TextView

    private lateinit var prenomCinLl: LinearLayout
    private lateinit var nomCinLl: LinearLayout
    private lateinit var dateCinLl: LinearLayout
    private lateinit var numberCinLl: LinearLayout
    private lateinit var adresseCinLl: LinearLayout
    private lateinit var sexeCinLl: LinearLayout

    private lateinit var prenomCinIcon: ImageView
    private lateinit var nomCinIcon: ImageView
    private lateinit var dateCinIcon: ImageView
    private lateinit var numberCinIcon: ImageView
    private lateinit var adresseCinIcon: ImageView
    private lateinit var sexeCinIcon: ImageView
    private lateinit var passportNumIcon: ImageView

    // Sejour
    private lateinit var nationaliteSejourResult: TextView
    private lateinit var validiteSejourResult: TextView

    // Passport
    private lateinit var passportNumResult: TextView
    private lateinit var passportNumLl: LinearLayout

    // Sim Card
    private lateinit var iccSimResult: TextView
    private lateinit var iccSimLl: LinearLayout
    private lateinit var mdnSimResult: TextView
    private lateinit var mdnSimLl: LinearLayout

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
        iccSimResult = findViewById(R.id.icc_sim_result)
        mdnSimResult = findViewById(R.id.mdn_sim_result)
        passportNumResult = findViewById(R.id.passport_num_result)

        // Layout of the context
        prenomCinLl = findViewById(R.id.prenom_cin_ll)
        nomCinLl = findViewById(R.id.nom_cin_ll)
        dateCinLl = findViewById(R.id.date_cin_ll)
        numberCinLl = findViewById(R.id.number_cin_ll)
        adresseCinLl = findViewById(R.id.adresse_cin_ll)
        sexeCinLl = findViewById(R.id.sexe_cin_ll)

        passportNumLl = findViewById(R.id.passport_num_ll)

        iccSimLl = findViewById(R.id.icc_sim_ll)
        mdnSimLl = findViewById(R.id.mdn_sim_ll)

        // Images
        prenomCinIcon = findViewById(R.id.prenom_cin_icon)
        nomCinIcon = findViewById(R.id.nom_cin_icon)
        dateCinIcon = findViewById(R.id.date_cin_icon)
        numberCinIcon = findViewById(R.id.number_cin_icon)
        adresseCinIcon = findViewById(R.id.adresse_cin_icon)
        sexeCinIcon = findViewById(R.id.sexe_cin_icon)
        passportNumIcon = findViewById(R.id.passport_num_icon)

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
                "passport" -> setPassportView(hashMap)
                "sim" -> setSimView(hashMap)
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
    private fun setCinView(resultCin: HashMap<String, String>) {
        showTextCin()

        if (resultCin["FirstName"] != null)
            prenomCinResult.text = resultCin["FirstName"].toString()
        else prenomCinIcon.setImageResource(R.drawable.warning)

        if (resultCin["LastName"] != null)
            nomCinResult.text = resultCin["LastName"].toString()
        else nomCinIcon.setImageResource(R.drawable.warning)

        if (resultCin["DOB"] != null)
            dateCinResult.text = resultCin["DOB"].toString()
        else dateCinIcon.setImageResource(R.drawable.warning)

        if (resultCin["CIN"] != null)
            numberCinResult.text = resultCin["CIN"].toString()
        else numberCinIcon.setImageResource(R.drawable.warning)

        if (resultCin["Sexe"] != null)
            sexeCinResult.text = resultCin["Sexe"].toString()
        else sexeCinIcon.setImageResource(R.drawable.warning)

        if (resultCin["Adresse"] != null)
            adresseCinResult.text = resultCin["Adresse"].toString()
        else adresseCinIcon.setImageResource(R.drawable.warning)
    }

    /*
     * Function to show cin result part
     */
    private fun showTextCin() {
        prenomCinLl.visibility = View.VISIBLE
        nomCinLl.visibility = View.VISIBLE
        dateCinLl.visibility = View.VISIBLE
        numberCinLl.visibility = View.VISIBLE
        adresseCinLl.visibility = View.VISIBLE
        sexeCinLl.visibility = View.VISIBLE
    }

    /**
     *  Function to set the value of sejour result
     */
    private fun setSejourView(resultSejour: HashMap<String, String>) {
        showTextCin() // same field like cin part
        prenomCinResult.text = resultSejour["FirstName"].toString()
        nomCinResult.text = resultSejour["LastName"].toString()
        dateCinResult.text = resultSejour["DOB"].toString()
        numberCinResult.text = resultSejour["CIN"].toString()
        adresseCinResult.text = resultSejour["Adresse"].toString()
        sexeCinResult.text = resultSejour["Sexe"].toString()
    }

    /**
     *  Function to set the value of passport result
     */
    private fun setPassportView(resultPassport: HashMap<String, String>) {
        showTextPassport()
        if (resultPassport["FirstName"] != null)
            prenomCinResult.text = resultPassport["FirstName"].toString()
        else prenomCinIcon.setImageResource(R.drawable.warning)

        if (resultPassport["LastName"] != null)
            nomCinResult.text = resultPassport["LastName"].toString()
        else nomCinIcon.setImageResource(R.drawable.warning)

        if (resultPassport["DOB"] != null)
            dateCinResult.text = resultPassport["DOB"].toString()
        else dateCinIcon.setImageResource(R.drawable.warning)

        if (resultPassport["CIN"] != null)
            numberCinResult.text = resultPassport["CIN"].toString()
        else numberCinIcon.setImageResource(R.drawable.warning)

        if (resultPassport["Sexe"] != null)
            sexeCinResult.text = resultPassport["Sexe"].toString()
        else sexeCinIcon.setImageResource(R.drawable.warning)

        if (resultPassport["Passport"] != null)
            passportNumResult.text = resultPassport["Passport"].toString()
        else passportNumIcon.setImageResource(R.drawable.warning)
    }

    /*
     * Function to show sim result part
     */
    private fun showTextPassport() {
        showTextCin()
        adresseCinLl.visibility = View.GONE // Like Cin view without adress
        passportNumLl.visibility = View.VISIBLE
    }

    /**
     *  Function to set the value of sim result
     */
    private fun setSimView(resultSim: HashMap<String, String>) {
        showTextSim()
        iccSimResult.text = resultSim["ICC"].toString()
        mdnSimResult.text = resultSim["MDN"].toString()
    }

    /*
     * Function to show sim result part
     */
    private fun showTextSim() {
        iccSimLl.visibility = View.VISIBLE
        mdnSimLl.visibility = View.VISIBLE
    }
}
