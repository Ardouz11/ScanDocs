package ocr.mobileVision.scanDocs

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
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
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.properties.Delegates

class ScanBackSejour : AppCompatActivity() {

    private var mCameraSource by Delegates.notNull<CameraSource>()
    private var textRecognizer by Delegates.notNull<TextRecognizer>()
    private lateinit var tv_result: TextView
    private lateinit var surface_camera_preview: SurfaceView
    private val permissionRequestCamera = 100
    private lateinit var start: ImageView
    var hashMap = HashMap<String, String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_back_sejour)

        val anim: LottieAnimationView
        val viewBg: View

        anim = findViewById(R.id.animationView)
        viewBg = findViewById(R.id.bg_onLoad)
        anim.visibility = View.GONE
        viewBg.visibility = View.GONE

        tv_result = findViewById(R.id.tv_result)
        start = findViewById(R.id.capture)
        surface_camera_preview = findViewById(R.id.surface_camera_preview)
        val extras = intent.extras
        startCameraSource()
        start.setOnClickListener {
            anim.visibility = View.VISIBLE
            viewBg.visibility = View.VISIBLE

            val intent = Intent(this, DataExtracted::class.java)
            textRecognizer.setProcessor(object : Detector.Processor<TextBlock> {
                override fun release() {
                    mCameraSource.stop()
                    intent.putExtra("dataCIN", hashMap)
                    intent.putExtra("fromActivity", "sejour")
                    startActivity(intent)
                }

                override fun receiveDetections(detections: Detector.Detections<TextBlock>) {
                    val items = detections.detectedItems

                    if (items.size() <= 0) {
                        return
                    }

                    tv_result.post {
                        if (extras != null) {
                            val intent = getIntent()
                            hashMap = intent.getSerializableExtra("frontData") as HashMap<String, String>
                            Log.d("hashback", hashMap.toString())
                        }

                        for (i in 0 until items.size()) {
                            val item = items.valueAt(i)
                            if (Pattern.matches("I[A-Z0-9<\\s]+", item.value) && item.value.length> 20) {
                                val match = item.value.replace(" ", "").drop(15)
                                val pLineOne = Pattern.compile("[A-Z]+|\\d+")
                                val mLineOne: Matcher = pLineOne.matcher(match)
                                var allMatchesLineOne: ArrayList<String> = ArrayList()
                                while (mLineOne.find()) {
                                    allMatchesLineOne.add(mLineOne.group())
                                }
                                Log.d("test", allMatchesLineOne.toString())
                                if (hashMap.get("CIN") != allMatchesLineOne[0] + allMatchesLineOne[1]) {
                                    hashMap.put("CIN", allMatchesLineOne[0] + allMatchesLineOne[1])
                                }
                                if (hashMap.get("Prenom") != allMatchesLineOne.last()) {
                                    hashMap.put("Prenom", allMatchesLineOne.last())
                                }

                                if (!hashMap.containsKey("Sexe")) {
                                    hashMap.put("Sexe", allMatchesLineOne[3].takeLast(1))
                                }
                                var string = StringBuilder()
                                if (allMatchesLineOne.size> 7) {
                                    for (i in 7 until allMatchesLineOne.size - 1) {

                                        string.append(allMatchesLineOne[i] + " ")
                                    }
                                    if (hashMap.get("Nom") != string.toString()) {
                                        hashMap.put("Nom", string.toString())
                                    }
                                }
                                if (allMatchesLineOne[2].take(2).toInt() <40) {
                                    hashMap.put("DOB", allMatchesLineOne[2].take(6).takeLast(2) + "/" + allMatchesLineOne[2].take(4).takeLast(2) + "/20" + allMatchesLineOne[2].take(2))
                                } else {
                                    hashMap.put("DOB", allMatchesLineOne[2].take(6).takeLast(2) + "/" + allMatchesLineOne[2].take(4).takeLast(2) + "/19" + allMatchesLineOne[2].take(2))
                                }
                            }
                            if (!hashMap.containsKey("Sexe")) {
                                if (Pattern.matches("Sexe.*[MF]", item.value)) {
                                    hashMap.put("Sexe", item.value.replace("Sexe", ""))
                                }
                                if (Pattern.matches("[MF]", item.value)) {
                                    hashMap.put("Sexe", item.value)
                                }
                            }
                            if (Pattern.matches("Adresse.*", item.value)) {
                                hashMap.put("Adresse", item.value.toUpperCase().replace("ADRESSE", ""))
                            }
                        }
                        release()
                    }
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()

        var anim: LottieAnimationView
        var viewBg: View

        anim = findViewById(R.id.animationView)
        viewBg = findViewById(R.id.bg_onLoad)
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

        surface_camera_preview.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
            }

            override fun surfaceDestroyed(p0: SurfaceHolder?) {
                mCameraSource.stop()
            }

            @SuppressLint("MissingPermission")
            override fun surfaceCreated(p0: SurfaceHolder?) {
                try {
                    if (isCameraPermissionGranted()) {
                        mCameraSource.start(surface_camera_preview.holder)
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
                mCameraSource.start(surface_camera_preview.holder)
            } else {
                toast("Permission need to grant")
                finish()
            }
        }
    }
}
