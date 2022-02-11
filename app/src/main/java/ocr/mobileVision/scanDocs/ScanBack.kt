package ocr.mobileVision.scanDocs

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import com.orhanobut.logger.Logger
import org.jetbrains.anko.toast
import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.properties.Delegates

class ScanBack : AppCompatActivity() {

    private var mCameraSource by Delegates.notNull<CameraSource>()
    private var textRecognizer by Delegates.notNull<TextRecognizer>()
    private lateinit var tvResult: TextView
    private lateinit var surfaceCameraPreview: SurfaceView
    private val permissionRequestCamera = 100
    private lateinit var start: ImageView
    private lateinit var extractLabel: TextView

    private var hashMap = HashMap<String, String>()
    private var extras: Bundle? = null
    private var size = 0
    private val date = 22

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_back)

        val anim: LottieAnimationView = findViewById(R.id.animationView)
        val viewBg: View = findViewById(R.id.bg_onLoad)
        extractLabel = findViewById(R.id.extract_label)

        anim.visibility = View.GONE
        viewBg.visibility = View.GONE
        extractLabel.visibility = View.GONE

        tvResult = findViewById(R.id.tv_result)
        start = findViewById(R.id.capture)
        surfaceCameraPreview = findViewById(R.id.surface_camera_preview)
        extras = intent.extras
        startCameraSource()
        start.setOnClickListener {
            anim.visibility = View.VISIBLE
            viewBg.visibility = View.VISIBLE
            extractLabel.visibility = View.VISIBLE
            anim.playAnimation()

            val intent = Intent(this, DataExtracted::class.java)
            textRecognizer.setProcessor(object : Detector.Processor<TextBlock> {
                override fun release() {
                    println("TODO")
                }

                override fun receiveDetections(detections: Detector.Detections<TextBlock>) {
                    val items = detections.detectedItems
                    if (items.size() <= 0) {
                        return
                    }
                    process(items, intent)
                }
            }
            )
        }
    }
    private fun process(items: SparseArray<TextBlock>?, intent: Intent) {
        if (extras != null) {
            val intent = getIntent()
            hashMap = intent.getSerializableExtra("frontData") as HashMap<String, String>
        }
        tvResult.post {
            for (i in 0 until items!!.size()) {
                val item = items.valueAt(i)
                val flagMatchMRZ = Pattern.matches("I[A-Z0-9<\\s]+", item.value)
                processMRZ(flagMatchMRZ, item)
                val flagMatchAddress = Pattern.matches("Adresse.*", item.value)
                processAddress(item, flagMatchAddress)
                val flagMatchSex = Pattern.matches("Sexe.*[MF]|[MF]", item.value)
                processSex(item, flagMatchSex)
            }
            releaseCam(intent)
        }
    }

    private fun processMRZ(flagMatchMRZ: Boolean, item: TextBlock?) {
        if (flagMatchMRZ && item!!.value.length> 20) {
            val match = item.value.replace(" ", "")
            val chunked = match.replace(" ", "").chunked(30)
            val pLineOne = Pattern.compile("[A-Z]+|\\d+")
            val pLineThree = Pattern.compile("[A-Z]+")
            if (chunked.size> 2) {
                val mLineOne: Matcher = pLineOne.matcher(chunked[0].replace(" ", "").drop(15))
                val mLineTwo: Matcher = pLineOne.matcher(chunked[1].replace(" ", "").take(15))
                val mLineThree: Matcher = pLineThree.matcher(chunked[2].replace(" ", ""))
                processLineOne(mLineOne)
                processLineTwo(mLineTwo)
                processLineThree(mLineThree)
            }
        }
    }

    private fun processLineThree(mLineThree: Matcher) {
        val allMatchesLineThree: ArrayList<String> = ArrayList()
        while (mLineThree.find()) {
            allMatchesLineThree.add(mLineThree.group())
        }
        this.size = allMatchesLineThree.size
        val flagMatchFirstname = hashMap["FirstName"] != allMatchesLineThree.last()
        processFirstName(flagMatchFirstname, allMatchesLineThree)
        processLastName(allMatchesLineThree)
    }

    private fun processLineTwo(mLineTwo: Matcher) {
        val allMatchesLineTwo: ArrayList<String> = ArrayList()
        while (mLineTwo.find()) {
            allMatchesLineTwo.add(mLineTwo.group())
        }
        Log.d("chunk", allMatchesLineTwo.toString())
        if (!hashMap.containsKey("Sexe")) {
            hashMap["Sexe"] = allMatchesLineTwo[1].takeLast(1)
        }
        if (allMatchesLineTwo[0].take(2).toInt() <date) {
            hashMap["DOB"] =
                allMatchesLineTwo[0].take(6).takeLast(2) + "/" + allMatchesLineTwo[0].take(4).takeLast(2) + "/20" + allMatchesLineTwo[0].take(2)
        } else {
            hashMap["DOB"] =
                allMatchesLineTwo[0].take(6).takeLast(2) + "/" + allMatchesLineTwo[0].take(4).takeLast(2) + "/19" + allMatchesLineTwo[0].take(2)
        }
    }

    private fun processLineOne(mLineOne: Matcher) {
        val allMatchesLineOne: ArrayList<String> = ArrayList()
        while (mLineOne.find()) {
            allMatchesLineOne.add(mLineOne.group())
        }
        Log.d("chunk", allMatchesLineOne.toString())
        val flagMatchCIN = hashMap["CIN"] != allMatchesLineOne[0] + allMatchesLineOne[1]
        processCIN(flagMatchCIN, allMatchesLineOne[0] + allMatchesLineOne[1])
    }

    private fun processFirstName(flagMatchFirstname: Boolean, allMatchesLineOne: ArrayList<String>) {
        while (allMatchesLineOne.remove("K")) {
            this.size = allMatchesLineOne.size
        }
        Log.d("chunk", allMatchesLineOne.toString())
        if (flagMatchFirstname) {
            hashMap["FirstName"] = allMatchesLineOne.last()
        }
    }

    private fun processLastName(allMatchesLineOne: ArrayList<String>) {
        val string = StringBuilder()
        for (i in 0 until this.size - 1) {
            string.append(allMatchesLineOne[i] + " ")
        }

        if (hashMap["LastName"] != string.toString()) {
            hashMap["LastName"] = string.toString()
        }
    }
    private fun processCIN(flagMatchCIN: Boolean, s: String) {
        if (flagMatchCIN) {
            hashMap["CIN"] = s
        }
    }

    private fun processSex(item: TextBlock?, flagMatchSex: Boolean) {
        if (flagMatchSex && !hashMap.containsKey("Sexe")) {
            hashMap["Sexe"] = item!!.value.replace("Sexe", "")
        }
    }

    private fun processAddress(item: TextBlock?, flagMatchAddress: Boolean) {
        if (flagMatchAddress) {
            hashMap["Adresse"] = item!!.value.toUpperCase(Locale.getDefault()).replace("ADRESSE ", "")
        }
    }

    private fun releaseCam(intent: Intent) {
        mCameraSource.stop()
        intent.putExtra("dataCIN", hashMap)
        intent.putExtra("fromActivity", "cin")
        startActivity(intent)
    }
    override fun onResume() {
        super.onResume()

        val anim: LottieAnimationView = findViewById(R.id.animationView)
        val viewBg: View = findViewById(R.id.bg_onLoad)
        anim.visibility = View.GONE
        viewBg.visibility = View.GONE
    }

    private fun startCameraSource() {

        //  Create text Recognizer
        textRecognizer = TextRecognizer.Builder(this).build()

        if (!textRecognizer.isOperational) {
            toast("Dependencies are not loaded yet...please try after few moment!!")
            Logger.d("Dependencies are downloading....try after few moment")
            return
        }

        //  Init camera source to use high resolution and auto focus
        mCameraSource = CameraSource.Builder(applicationContext, textRecognizer)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .setRequestedPreviewSize(1280, 1024)
            .setAutoFocusEnabled(true)
            .setRequestedFps(2.0f)
            .build()

        surfaceCameraPreview.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
                println("TODO")
            }

            override fun surfaceDestroyed(p0: SurfaceHolder?) {
                mCameraSource.stop()
            }

            @SuppressLint("MissingPermission")
            override fun surfaceCreated(p0: SurfaceHolder?) {
                try {
                    if (isCameraPermissionGranted()) {
                        mCameraSource.start(surfaceCameraPreview.holder)
                    } else {
                        requestForPermission()
                    }
                } catch (e: Exception) {
                    toast("Error:  ${e.message}")
                }
            }
        })
    }

    fun isCameraPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun requestForPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), permissionRequestCamera)
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode != permissionRequestCamera) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (isCameraPermissionGranted()) {
                mCameraSource.start(surfaceCameraPreview.holder)
            } else {
                toast("Permission need to grant")
                finish()
            }
        }
    }
}
