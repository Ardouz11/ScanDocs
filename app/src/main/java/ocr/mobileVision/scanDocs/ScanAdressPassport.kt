package ocr.mobileVision.scanDocs
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import com.orhanobut.logger.Logger
import org.jetbrains.anko.toast
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.properties.Delegates





class ScanAdressPassport : AppCompatActivity() {

    private var mCameraSource by Delegates.notNull<CameraSource>()
    private var textRecognizer by Delegates.notNull<TextRecognizer>()
    private lateinit var tvResult:TextView
    private lateinit var surface_camera_preview:SurfaceView
    val stringBuilder = StringBuilder()
    val stringBuilderTwo = StringBuilder()
    private val PERMISSION_REQUEST_CAMERA = 100
    private lateinit var button: Button
    private lateinit var buttonFront: Button
    private lateinit var buttonNext: Button
    val pattern = Pattern.compile("[^A-Z0-9 ]")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_adress_passport)
        tvResult=findViewById(R.id.tv_result)
        button=findViewById(R.id.button)
        buttonFront=findViewById(R.id.buttonFront)
        surface_camera_preview=findViewById(R.id.surface_camera_preview)
        startCameraSource()
        val extras = intent.extras
        buttonFront.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Info of Front Side")
            builder.setMessage(stringBuilder.toString())
            builder.setPositiveButton(android.R.string.yes) { _, _ ->
                Toast.makeText(applicationContext,
                    android.R.string.yes, Toast.LENGTH_SHORT).show()
            }

            builder.setNegativeButton(android.R.string.no) { _, _ ->
                Toast.makeText(applicationContext,
                    android.R.string.no, Toast.LENGTH_SHORT).show()
            }

            builder.show()
        }

        button.setOnClickListener {
            textRecognizer.setProcessor(object : Detector.Processor<TextBlock> {
                override fun release() {}

                override fun  receiveDetections(detections: Detector.Detections<TextBlock>) {
                    val items = detections.detectedItems
                    if (items.size() <= 0) {
                        return
                    }
                    tvResult.post {
                        stringBuilder.setLength(0)
                        if (extras != null) {
                            val value = extras.getString("MRZData")
                            stringBuilder.append(value)
                        }
                        for (i in 0 until items.size()) {
                            val item = items.valueAt(i)
                            if(Pattern.matches("^[A-Z0-9 ]+\$",item.value)&&item.value.split(" ").size>4){
                                var matcher = pattern.matcher(items.valueAt(i+1).value)
                                var str=matcher.replaceAll("")
                                Log.i("test",item.value+" "+str.substringBefore("MAR "))
                                stringBuilder.append("Adresse : " +item.value+" "+str.substringBefore("MAR ")+"\n")
                            }

                                }
                            }
                        }


                   // mCameraSource.stop()


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
