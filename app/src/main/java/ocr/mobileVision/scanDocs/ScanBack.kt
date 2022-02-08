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
    var hashMap=HashMap<String,String>()
      var extras:Bundle?=null
    var size=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_back)
        val anim: LottieAnimationView
        val viewBg: View
        anim = findViewById(R.id.animationView)
        viewBg = findViewById(R.id.bg_onLoad)
        anim.visibility = View.GONE
        viewBg.visibility = View.GONE
        tv_result = findViewById(R.id.tv_result)
        start = findViewById(R.id.capture)
        surface_camera_preview = findViewById(R.id.surface_camera_preview)
        extras = intent.extras
        startCameraSource()
        start.setOnClickListener {
            anim.visibility = View.VISIBLE
            viewBg.visibility = View.VISIBLE

            val intent = Intent(this, DataExtracted::class.java)
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
                }
            )
        }
    }
    private fun process(items: SparseArray<TextBlock>?, intent: Intent) {
        if (extras != null) {
            val intent = getIntent()
            hashMap =intent.getSerializableExtra("frontData") as HashMap<String, String>

        }
        tv_result.post {
            for (i in 0 until items!!.size()) {
                val item = items.valueAt(i)
                var flagMatchMRZ=Pattern.matches("I[A-Z0-9<\\s]+", item.value)
                processMRZ(flagMatchMRZ,item)
                var flagMatchAddress=Pattern.matches("Adresse.*", item.value)
                processAddress(item,flagMatchAddress)
                var flagMatchSex=Pattern.matches("Sexe.*[MF]|[MF]", item.value)
                processSex(item,flagMatchSex)
            }
            releaseCam(intent)
        } }

    private fun processMRZ(flagMatchMRZ: Boolean, item: TextBlock?) {
        if (flagMatchMRZ&&item!!.value.length>20) {
            val match=item.value.replace(" ","").drop(15)
            val pLineOne = Pattern.compile("[A-Z]+|\\d+")
            val mLineOne: Matcher = pLineOne.matcher(match)
            var allMatchesLineOne: ArrayList<String> = ArrayList()
            while (mLineOne.find()) {
                allMatchesLineOne.add(mLineOne.group())
            }
            this.size=allMatchesLineOne.size
            Log.d("debug", allMatchesLineOne.toString())
            var flagMatchCIN=hashMap.get("CIN")!=allMatchesLineOne[0] + allMatchesLineOne[1]
            processCIN(flagMatchCIN,allMatchesLineOne[0] + allMatchesLineOne[1])
            var flagMatchFname=hashMap.get("Prenom")!=allMatchesLineOne.last()
            processFname(flagMatchFname,allMatchesLineOne)

            var flagMatch=allMatchesLineOne.size>7
            processLname(flagMatch,allMatchesLineOne)

        }
    }

    private fun processFname(flagMatchFname: Boolean, allMatchesLineOne: ArrayList<String>) {
        if(flagMatchFname){
            if(allMatchesLineOne.last()=="K"){
                this.size=this.size-1
                hashMap.put("Prenom", allMatchesLineOne[this.size-1])
            }
            hashMap.put("Prenom", allMatchesLineOne.last())
        }
    }

    private fun processLname(flagMatch: Boolean, allMatchesLineOne: ArrayList<String>) {
        var string = StringBuilder()
        if(flagMatch) {
            for (i in 7 until this.size-1) {
                string.append(allMatchesLineOne[i] + " ")
            }
            if(!hashMap.containsKey("Sexe")){
                hashMap.put("Sexe",allMatchesLineOne[3].takeLast(1))
            }
            if (hashMap.get("Nom") != string.toString()) {
                hashMap.put("Nom", string.toString())
            }
            if(allMatchesLineOne[2].take(2).toInt()<40){
                hashMap.put("DOB" , allMatchesLineOne[2].take(6).takeLast(2) + "/" + allMatchesLineOne[2].take(4).takeLast(2) + "/20" + allMatchesLineOne[2].take(2))
            }
            else{
                hashMap.put("DOB" , allMatchesLineOne[2].take(6).takeLast(2) + "/" + allMatchesLineOne[2].take(4).takeLast(2) + "/19" + allMatchesLineOne[2].take(2) )

            }
        }
    }

    private fun processCIN(flagMatchCIN: Boolean, s: String) {
        if(flagMatchCIN){
            hashMap.put("CIN" , s ) 
        }
       
    }

    private fun processSex(item: TextBlock?, flagMatchSex: Boolean) {
            if (flagMatchSex&&!hashMap.containsKey("Sexe")) {
                hashMap.put("Sexe",item!!.value.replace("Sexe",""))
        }
    }

    private fun processAddress(item: TextBlock?, flagMatchAddress: Boolean) {
        if (flagMatchAddress) {
            hashMap.put("Adresse",item!!.value.toUpperCase().replace("ADRESSE",""))
        }
    }

    private fun releaseCam(intent: Intent) {
        mCameraSource.stop()
        intent.putExtra("dataCIN", hashMap)
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
