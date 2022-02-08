package ocr.mobileVision.scanDocs
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
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
    private lateinit var surface_camera_preview: SurfaceView
    val hashMap=HashMap<String,String>()
    private val PERMISSION_REQUEST_CAMERA = 100
    private var flagName:Boolean = false
    private lateinit var start: ImageView
    private  var regex:String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var anim: LottieAnimationView
        var viewBg: View
        anim = findViewById(R.id.animationView)
        viewBg = findViewById(R.id.bg_onLoad)
        anim.visibility = View.GONE
        viewBg.visibility = View.GONE
        tvResult = findViewById(R.id.tv_result)
        start = findViewById(R.id.capture)
        surface_camera_preview = findViewById(R.id.surface_camera_preview)
        startCameraSource()
        val ai = packageManager.getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA)
        val bundle = ai.metaData
        regex = bundle.getString("regexIDCard")
        start.setOnClickListener {
            anim.visibility = View.VISIBLE
            viewBg.visibility = View.VISIBLE
            val intent = Intent(this, ScanBack::class.java)
            textRecognizer.setProcessor(object : Detector.Processor<TextBlock> {
                override fun release() {
                    //TODO
                }
                override fun receiveDetections(detections: Detector.Detections<TextBlock>) {
                    val items = detections.detectedItems
                    if (items.size() <= 0) {
                        return
                    }
                    process(items,intent)

                }
            })
        }
    }

    private fun process(items: SparseArray<TextBlock>?,intent: Intent) {
        tvResult.post {
            this.flagName = true
            for (i in 0 until items!!.size()) {
                val item = items.valueAt(i)
                if (!Pattern.matches(regex!!.toRegex().toString(), item.value)) {
                    var flagMatchDOB=Pattern.matches("[0-9].*[0-9]", item.value)
                    processDOB(item,i,flagMatchDOB)
                    var flagMatchCIN=Pattern.matches("^[A-Z]+[0-9]+", item.value)
                    processCIN(flagMatchCIN,item)
                    var flagMatchFLname=Pattern.matches("^[A-Z]+", item.value)
                    processFLname(item,flagMatchFLname)

                }
            }
            releaseCam(intent) } }

    private fun processFLname(item: TextBlock?,flagMatchFLname:Boolean) {
        if (flagMatchFLname && item!!.value.toString().length> 2) {
            if (this.flagName&&!hashMap.containsKey("Prenom")) {
                hashMap.put("Prenom",item.value)
                this.flagName = false
            } else {
                if (!hashMap.containsKey("Nom")) {
                    hashMap.put("Nom",item.value)
                    this.flagName = true
                }
            }
        }
    }

    private fun processCIN(flagMatchCIN: Boolean, item: TextBlock?) {
        if (flagMatchCIN&&!hashMap.containsKey("CIN")) {
            hashMap.put("CIN",item!!.value)
        }

    }

    private fun processDOB(item: TextBlock?, i: Int, flagMatchDOB: Boolean) {
        if ( flagMatchDOB && item!!.value.length> 5 && i <7&&!hashMap.containsKey("DOB")) {
            hashMap.put("DOB",item.value)
        }
    }

    private fun releaseCam(intent: Intent) {
        mCameraSource.stop()
        intent.putExtra("frontData", hashMap)
        startActivity(intent)
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
                //TODO
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