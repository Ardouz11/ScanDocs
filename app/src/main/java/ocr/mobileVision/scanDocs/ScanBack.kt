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

class ScanBack : AppCompatActivity() {

    private var mCameraSource by Delegates.notNull<CameraSource>()
    private var textRecognizer by Delegates.notNull<TextRecognizer>()
    private lateinit var tv_result: TextView
    private lateinit var surface_camera_preview: SurfaceView
    private val PERMISSION_REQUEST_CAMERA = 100
    private lateinit var start: ImageView
    val stringBuilder=StringBuilder()
    var hashMap=HashMap<String,String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_back)

        var anim: LottieAnimationView
        var viewBg: View

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
                             hashMap =intent.getSerializableExtra("frontData") as HashMap<String, String>
                            Log.d("hashback",hashMap.toString())

                        }

                        for (i in 0 until items.size()) {
                            val item = items.valueAt(i)
                            Log.d("items",item.value)
                             if (Pattern.matches("[A-Z0-9<\\s]+",item.value)&&item.value.length>20) {
                                 val match=item.value.replace(" ","")
                                 Log.d("test", match)
                                 var pLineOne = Pattern.compile("[A-Z]+|\\d+")
                                 var mLineOne: Matcher = pLineOne.matcher(match)
                                 var allMatchesLineOne: ArrayList<String> = ArrayList()
                                 while (mLineOne.find()) {
                                     allMatchesLineOne.add(mLineOne.group())
                                 }
                                 Log.d("test", allMatchesLineOne.toString())
                                 hashMap.put("CIN " , allMatchesLineOne[4] + allMatchesLineOne[5] )
                                 hashMap.put("Prenom ", allMatchesLineOne.last() )
                                 if(!stringBuilder.toString().contains("Sexe")){
                                     hashMap.put("Sexe",allMatchesLineOne[8].takeLast(1))
                                 }
                                 var string = StringBuilder()
                                 for (i in 12 until allMatchesLineOne.size - 1) {

                                     string.append(allMatchesLineOne[i] + " ")
                                 }
                                 hashMap.put("Nom",string.toString())
                                 if(allMatchesLineOne[6].take(2).toInt()<40){
                                     hashMap.put("DOB" , allMatchesLineOne[6].take(6).takeLast(2) + "/" + allMatchesLineOne[6].take(4).takeLast(2) + "/20" + allMatchesLineOne[6].take(2))
                                 }
                                 else{
                                     hashMap.put("DOB" , allMatchesLineOne[6].take(6).takeLast(2) + "/" + allMatchesLineOne[6].take(4).takeLast(2) + "/19" + allMatchesLineOne[6].take(2) )

                                 }
                                 hashMap.put("END Of Val" , allMatchesLineOne[7].take(6).takeLast(2) + "/" + allMatchesLineOne[7].take(4).takeLast(2) + "/20" + allMatchesLineOne[7].take(2))

                                 /*    stringBuilder.append("CIN : " + allMatchesLineOne[allMatchesLineOne.size-1]+allMatchesLineOne.last()  + "\n")
                                   allMatchesLineOne[1] = allMatchesLineOne[1].drop(3)
                                    Log.d("list1", allMatchesLineOne.toString())

                                    var pLineTwo = Pattern.compile("[A-Z]+|\\d+")
                                    var mLineTwo: Matcher = pLineTwo.matcher(items.valueAt(i + 1).value)
                                    var allMatches: ArrayList<String> = ArrayList()
                                    while (mLineTwo.find()) {
                                        allMatches.add(mLineTwo.group())
                                    }
                                    Log.d("list1", allMatches.toString())
                                    var pLineThree = Pattern.compile("[A-Z]+|\\d+")
                                    var mLineThree: Matcher = pLineThree.matcher(items.valueAt(i + 2).value)
                                    var allMatchesThree: ArrayList<String> = ArrayList()
                                    while (mLineThree.find()) {
                                        allMatchesThree.add(mLineThree.group())
                                    }
                                    Log.d("list1", allMatchesThree.toString())
                                    for (i in 1 until allMatchesThree.size - 1) {

                                        string.append(allMatchesThree[i] + " ")
                                    }
                                stringBuilder.append("Nom2: $string\n")
                                    stringBuilder.append("Prenom2 : "+ allMatchesLineOne.last() + "\n")
                                    if(allMatches[3].take(2).toInt()<40){
                                        stringBuilder.append("DOB2 : " + allMatches[0].take(6).takeLast(2) + "/" + allMatches[0].take(4).takeLast(2) + "/20" + allMatches[0].take(2) + "\n")
                                    }
                                    else{
                                        stringBuilder.append("DOB2 : " + allMatches[0].take(6).takeLast(2) + "/" + allMatches[0].take(4).takeLast(2) + "/19" + allMatches[0].take(2) + "\n")

                                    }
                                    stringBuilder.append("Sexe2 : " + allMatches[1].takeLast(1) + "\n")
                                    stringBuilder.append("END Of Val : " + allMatches[2].take(6).takeLast(2) + "/" + allMatches[2].take(4).takeLast(2) + "/20" + allMatches[2].take(2) + "\n")
                             */ }
                                if(!hashMap.containsKey("Sexe")){
                                if (Pattern.matches("Sexe.*[MF]", item.value)) {
                                    hashMap.put("Sexe",item.value)
                                }
                                if (Pattern.matches("[MF]", item.value)) {
                                    hashMap.put("Sexe",item.value)
                                }
                            }
                            if (Pattern.matches("Adresse.*", item.value)) {
                                hashMap.put("Adresse",item.value.toUpperCase())
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
