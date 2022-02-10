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

class ScanFrontPassport : AppCompatActivity() {

    private var mCameraSource by Delegates.notNull<CameraSource>()
    private var textRecognizer by Delegates.notNull<TextRecognizer>()
    private lateinit var tvResult: TextView
    private lateinit var surface_camera_preview: SurfaceView
    var string: String = ""
    private val PERMISSION_REQUEST_CAMERA = 100
    val hashMap = HashMap<String, String>()
    private lateinit var start: ImageView
    private var size = 0
    val pattern = Pattern.compile("[^A-Z0-9 ]")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_front_passport)
        tvResult = findViewById(R.id.tv_result)
        start = findViewById(R.id.capture)

        var anim: LottieAnimationView
        var viewBg: View

        anim = findViewById(R.id.animationView)
        viewBg = findViewById(R.id.bg_onLoad)
        anim.visibility = View.GONE
        viewBg.visibility = View.GONE

        surface_camera_preview = findViewById(R.id.surface_camera_preview)
        startCameraSource()
        start.setOnClickListener {
            anim.visibility = View.VISIBLE
            viewBg.visibility = View.VISIBLE

            val intent = Intent(this, DataExtracted::class.java)
            textRecognizer.setProcessor(object : Detector.Processor<TextBlock> {
                override fun release() {}

                override fun receiveDetections(detections: Detector.Detections<TextBlock>) {
                    val items = detections.detectedItems
                    if (items.size() <= 0) {
                        return
                    }
                    tvResult.post {
                        for (i in 0 until items.size()) {
                            val item = items.valueAt(i)
                            if (Pattern.matches("P<.*", item.value)) {
                                var pLineOne = Pattern.compile("[A-Z]+")
                                var mLineOne: Matcher = pLineOne.matcher(item.value)
                                var allMatchesLineOne: ArrayList<String> = ArrayList()
                                while (mLineOne.find()) {
                                    allMatchesLineOne.add(mLineOne.group())
                                }
                                size = allMatchesLineOne.size
                                var string = StringBuilder()
                                if (allMatchesLineOne.remove("K")) {
                                    size = allMatchesLineOne.size
                                }
                                hashMap.put("FirstName", allMatchesLineOne[size-1])
                                allMatchesLineOne[1] = allMatchesLineOne[1].drop(3)
                                for (i in 1 until size - 1) {
                                    string.append(allMatchesLineOne[i] + " ")
                                }
                                hashMap.put("LastName", string.toString())
                                var pLineTwo = Pattern.compile("[A-Z]+|\\d+")
                                var mLineTwo: Matcher = pLineTwo.matcher(items.valueAt(i + 1).value)
                                var allMatches: ArrayList<String> = ArrayList()
                                while (mLineTwo.find()) {
                                    allMatches.add(mLineTwo.group())
                                }

                                hashMap.put("Passport", allMatches[0] + allMatches[1].dropLast(1))
                                hashMap.put("Nationality", allMatches[2])
                                if (allMatches[3].take(2).toInt() <40) {
                                    hashMap.put("DOB", allMatches[3].take(6).takeLast(2) + "/" + allMatches[3].take(4).takeLast(2) + "/20" + allMatches[3].take(2))
                                } else {
                                    hashMap.put("DOB", allMatches[3].take(6).takeLast(2) + "/" + allMatches[3].take(4).takeLast(2) + "/19" + allMatches[3].take(2))
                                }
                                hashMap.put("Sexe", allMatches[4].takeLast(1))
                                hashMap.put("END Of Val", allMatches[5].take(6).takeLast(2) + "/" + allMatches[5].take(4).takeLast(2) + "/20" + allMatches[5].take(2))
                                if (allMatches.size> 7) {
                                    hashMap.put("CIN", allMatches[6] + allMatches[7])
                                }
                            }
                        }
                        mCameraSource.stop()
                        intent.putExtra("dataCIN", hashMap)
                        intent.putExtra("fromActivity", "passport")
                        startActivity(intent)
                    }
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
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
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CAMERA)
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode != PERMISSION_REQUEST_CAMERA) {
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
