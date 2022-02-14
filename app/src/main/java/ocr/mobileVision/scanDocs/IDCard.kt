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
import java.util.regex.Pattern
import kotlin.properties.Delegates

class IDCard : AppCompatActivity() {
    private var mCameraSource by Delegates.notNull<CameraSource>()
    private var textRecognizer by Delegates.notNull<TextRecognizer>()
    private lateinit var tvResult: TextView
    private lateinit var surfaceCameraPreview: SurfaceView
    private val hashMap = HashMap<String, String>()
    private val permissionRequestCamera = 100
    private var flagName: Boolean = false
    private lateinit var start: ImageView
    private lateinit var extractLabel: TextView
    private var regex: String? = null
    private var tStart: Long? = null
    private var tEnd: Long? = 0
    private var k = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val anim: LottieAnimationView = findViewById(R.id.animationView)
        val viewBg: View = findViewById(R.id.bg_onLoad)
        extractLabel = findViewById(R.id.extract_label)

        anim.visibility = View.GONE
        viewBg.visibility = View.GONE
        extractLabel.visibility = View.GONE
        tvResult = findViewById(R.id.tv_result)
        start = findViewById(R.id.capture)
        surfaceCameraPreview = findViewById(R.id.surface_camera_preview)
        startCameraSource()
        val ai = packageManager.getApplicationInfo(this.packageName, PackageManager.GET_META_DATA)
        val bundle = ai.metaData
        regex = bundle.getString("regexIDCard")
        start.setOnClickListener {
            anim.visibility = View.VISIBLE
            viewBg.visibility = View.VISIBLE
            extractLabel.visibility = View.VISIBLE
            anim.playAnimation()

            val intent = Intent(this, ScanBack::class.java)
            textRecognizer.setProcessor(object : Detector.Processor<TextBlock> {
                override fun release() {
                    println("TODO")
                }
                override fun receiveDetections(detections: Detector.Detections<TextBlock>) {
                    tStart = System.currentTimeMillis()
                    val items = detections.detectedItems
                    if (items.size() <= 0) {
                        return
                    }
                    process(items, intent)
                }
            })
        }
    }

    private fun process(items: SparseArray<TextBlock>?, intent: Intent) {
        val helper = Helpers()
        val arrayList = helper.removeUnuseful(items, regex!!.toRegex().toString())
        tvResult.post {
            this.flagName = true
            for (i in 0 until arrayList.size) {
                val item = arrayList[i]
                val flagMatchDOB = Pattern.matches("[0-9].*[0-9]", item)
                processDOB(item, i, flagMatchDOB)
                val flagMatchCIN = Pattern.matches("^[A-Z]+[0-9]+", item)
                processCIN(flagMatchCIN, item)
                val flagMatchFLName = Pattern.matches("^[A-Z]+", item)
                processFLName(item, flagMatchFLName)
            }
            tEnd = System.currentTimeMillis()
            val tDelta: Long = this.tEnd!! - this.tStart!!
            val elapsedSeconds = tDelta / 1000.0
            if (k == 0) {
                k++
                Log.d("elapsed_front", elapsedSeconds.toString())
            }

            releaseCam(intent)
        }
    }

    private fun processFLName(item: String, flagMatchFLName: Boolean) {
        if (flagMatchFLName && item.length> 2) {
            if (this.flagName && !hashMap.containsKey("FirstName")) {
                hashMap["FirstName"] = item
                this.flagName = false
            } else {
                if (!hashMap.containsKey("LastName")) {
                    hashMap["LastName"] = item
                    this.flagName = true
                }
            }
        }
    }

    private fun processCIN(flagMatchCIN: Boolean, item: String) {
        if (flagMatchCIN && !hashMap.containsKey("CIN")) {
            hashMap["CIN"] = item
        }
    }

    private fun processDOB(item: String, i: Int, flagMatchDOB: Boolean) {
        if (flagMatchDOB && item.length> 5 && i <5 && !hashMap.containsKey("DOB")) {
            hashMap["DOB"] = item
        }
    }

    private fun releaseCam(intent: Intent) {
        mCameraSource.stop()
        intent.putExtra("frontData", hashMap)
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
