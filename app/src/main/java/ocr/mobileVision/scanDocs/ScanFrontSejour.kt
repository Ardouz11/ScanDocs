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
import java.util.regex.Pattern
import kotlin.properties.Delegates


class ScanFrontSejour : AppCompatActivity() {

    private var mCameraSource by Delegates.notNull<CameraSource>()
    private var textRecognizer by Delegates.notNull<TextRecognizer>()
    private lateinit var tvResult:TextView
    private lateinit var surface_camera_preview:SurfaceView
    val stringBuilder = StringBuilder()
    private val PERMISSION_REQUEST_CAMERA = 100
    private lateinit var button: Button
    private lateinit var buttonFront: Button
    private lateinit var buttonNext: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_front_sejour)
        tvResult=findViewById(R.id.tv_result)
        button=findViewById(R.id.button)
        buttonFront=findViewById(R.id.buttonFront)
        buttonNext=findViewById(R.id.buttonNext)
        surface_camera_preview=findViewById(R.id.surface_camera_preview)
        startCameraSource()
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

        buttonNext.setOnClickListener {
            val intent = Intent(this, ScanBackSejour::class.java)
            startActivity(intent)
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
                        var flagName = true
                        var flagCin=true
                        var flagDob=true
                        stringBuilder.setLength(0)
                        for (i in 0 until items.size()) {
                            val item = items.valueAt(i)

                            if(Pattern.matches("ROYAUM.*", item.value)
                                ||Pattern.matches("CARTE.*", item.value)
                                ||Pattern.matches(".*IMMATRICULA.*", item.value)
                                ||Pattern.matches(".*[ä].*",item.value)
                                ||Pattern.matches("[à].*",item.value)
                                ||Pattern.matches(".*[~!@#\$%^&*()_+'{}\\[\\]:;<>?-].*", item.value)
                            ){
                                Log.i("matches",item.value)
                            }
                            else {
                                /*  This patterns for matching DOB  */
                                    if(Pattern.matches("[0-9].*[0-9]",item.value)&&item.value.length>5){
                                        if(flagDob){
                                            if(!stringBuilder.contains("Date")){
                                             flagDob=false
                                        stringBuilder.append("Date de naissance est : " +item.value + "\n")
                                            }
                                    }

                                }
                                if(Pattern.matches("Valable du.*",item.value)){
                                        if(!stringBuilder.contains("Valable du")){
                                            stringBuilder.append("Valable du : " +items.valueAt(i+1).value + "\n")
                                        }


                                }
                                if(Pattern.matches("au.*",item.value)){
                                    if(!stringBuilder.contains("Valable jusqu'au")){
                                        stringBuilder.append("Valable jusqu'au : " +items.valueAt(i+1).value + "\n")
                                    }
                                }
                                if(Pattern.matches("National.*",item.value)){
                                    if(!stringBuilder.contains("Nationali")){
                                        stringBuilder.append(item.value + "\n")
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
                                if(flagCin){
                                    if(Pattern.matches("[A-Z].*[0-9].*",item.value)){
                                        if(!stringBuilder.contains("CIN")){
                                        stringBuilder.append("CIN est : " +item.value + "\n")
                                        flagCin=false
                                        }
                                    }}


                                /* This one for getting LName and FName */
                                if (Pattern.matches("[A-Z].*[A-Z]", item.value)&&item.value.toString().length>2) {
                                    if(flagName) {
                                        if(!stringBuilder.contains("Prenom")){
                                        stringBuilder.append("Prenom est : " +item.value + "\n")
                                        flagName=false
                                        }
                                    }
                                    else {
                                        if(!stringBuilder.contains("Nom")){
                                        stringBuilder.append("Nom est : " +item.value + "\n")
                                        flagName=true
                                        }
                                    }
                                }
                            }
                        }

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
