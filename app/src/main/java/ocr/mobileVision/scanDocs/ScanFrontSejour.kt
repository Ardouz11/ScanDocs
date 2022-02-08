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
import java.util.regex.Pattern
import kotlin.properties.Delegates

class ScanFrontSejour : AppCompatActivity() {

    private var mCameraSource by Delegates.notNull<CameraSource>()
    private var textRecognizer by Delegates.notNull<TextRecognizer>()
    private lateinit var tvResult: TextView
    private lateinit var surface_camera_preview: SurfaceView
    val hashMap=HashMap<String,String>()
    var string: String = ""
    private val PERMISSION_REQUEST_CAMERA = 100
    private lateinit var start: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_front_sejour)
        tvResult = findViewById(R.id.tv_result)
        start = findViewById(R.id.capture)

        var anim: LottieAnimationView
        var viewBg: View

        anim = findViewById(R.id.animationView)
        viewBg = findViewById(R.id.bg_onLoad)
        anim.visibility = View.GONE
        viewBg.visibility = View.GONE
        val ai = packageManager.getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA)
        val bundle = ai.metaData
        val regex = bundle.getString("regexIDCard")
        surface_camera_preview = findViewById(R.id.surface_camera_preview)
        startCameraSource()
        start.setOnClickListener {
            anim.visibility = View.VISIBLE
            viewBg.visibility = View.VISIBLE

            val intent = Intent(this, ScanBackSejour::class.java)
            textRecognizer.setProcessor(object : Detector.Processor<TextBlock> {
                override fun release() {

                    mCameraSource.stop()
                    intent.putExtra("frontData", hashMap)
                    startActivity(intent)
                }

                override fun receiveDetections(detections: Detector.Detections<TextBlock>) {
                    val items = detections.detectedItems

                    if (items.size() <= 0) {
                        return
                    }
                    tvResult.post {
                        var flagName = true
                        var flagDob = true
                        for (i in 0 until items.size()) {
                            val item = items.valueAt(i)
                            if (regex != null) {
                                if (Pattern.matches(regex.toRegex().toString(), item.value)
                                ) {
                                    Log.i("matches", item.value)
                                } else {
                                    /*  This pattern for matching DOB  */
                                    if (Pattern.matches("[0-9].*[0-9]", item.value) && item.value.length> 5) {
                                        if (flagDob) {
                                            if (!hashMap.containsKey("Date")) {
                                                flagDob = false
                                                hashMap.put("DOB" , item.value)
                                            }
                                        }
                                    }
                                    if (Pattern.matches("Valable du.*", item.value)) {
                                        if (!hashMap.containsKey("Valable du")) {
                                            hashMap.put("Valable du" , items.valueAt(i + 1).value)
                                        }
                                    }
                                    if (Pattern.matches("au.*", item.value)) {
                                        if (!hashMap.containsKey("Valable jusqu'au")) {
                                            hashMap.put("Valable jusqu'au", items.valueAt(i + 1).value)
                                        }
                                    }
                                    if (Pattern.matches("National.*", item.value)) {
                                        if (!hashMap.containsKey("Nationali")) {
                                            hashMap.put("Nationality",item.value.replace("Nationalité",""))
                                        }
                                    }

                                    /*
                                                        if(Pattern.matches("à.*",item.value)){
                                                            if(!stringBuilder.contains("POB ")) {
                                                                stringBuilder.append("POB " + item.value.replace("à", ": ") + "\n")
                                                            }
                                                        }*/
                                    /*  if(Pattern.matches("Vala.*[0-9]",item.value)){
                                                                if(!stringBuilder.contains("Valable jusqu'au")){
                                                                    val words = item.value.split("\\W+".toRegex())
                                                                    Log.d("words",words[-1])
                                                                    stringBuilder.append("Valable jusqu'au " +words[3]+"."+words[4]+"."+words[5]+"\n")
                                                                }
                                                            }
                                                            */

                                    /* This one for getting CIN */
                                    if (Pattern.matches("^[A-Z]+[0-9]+", item.value)) {
                                        if (!hashMap.containsKey("CIN")) {
                                            hashMap.put("CIN",item.value)
                                        }
                                    }

                                    /* This one for getting LName and FName */
                                    if (Pattern.matches("[A-Z].*[A-Z]", item.value) && item.value.toString().length> 2) {
                                        if (flagName) {
                                            if (!hashMap.containsKey("Prenom")) {
                                                hashMap.put("Prenom", item.value)
                                                flagName = false
                                            }
                                        } else {
                                            if (!hashMap.containsKey("Nom")) {
                                                hashMap.put("Nom" ,item.value)
                                                flagName = true
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        release()
                    }
                    // mCameraSource.stop()
                }
            })
        }
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
