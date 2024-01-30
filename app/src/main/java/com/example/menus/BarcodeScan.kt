package com.example.menus

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import android.widget.ScrollView
import com.example.menus.databinding.ActivityBarcodeScanBinding
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import java.io.IOException


class BarcodeScan : AppCompatActivity() {

    private lateinit var mBinding: ActivityBarcodeScanBinding

    private lateinit var barCodeDetector: BarcodeDetector
    private lateinit var  cameraSource: CameraSource

    var intentData = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityBarcodeScanBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mBinding.btnAction.setOnClickListener {

            val intentResultado = Intent()
            intentResultado.putExtra("barCode", mBinding.tvCode.text.toString())
            setResult(Activity.RESULT_OK, intentResultado)
            finish()
        }
    }

    private fun inicBc(){
        barCodeDetector = BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()
        cameraSource = CameraSource.Builder(this, barCodeDetector)
            .setRequestedPreviewSize(1080, 1920)
            .setAutoFocusEnabled(true)
            //.setFacing(CameraSource.CAMERA_FACING_FRONT)
            .build()

        mBinding.sfView!!.holder.addCallback(object : SurfaceHolder.Callback{
            @SuppressLint("MissingPermission")
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    cameraSource.start(mBinding.sfView!!.holder)

                }catch (e: IOException){
                    e.printStackTrace()
                }
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int,
            ) {

            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()
            }

        })

        barCodeDetector.setProcessor(object  : Detector.Processor<Barcode>{
            override fun release() {
                //Toast.makeText(applicationContext, "barcode scanner has been stopped", Toast.LENGTH_LONG).show()
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if(barcodes.size()!=0){
                    mBinding.tvCode!!.post {
                        mBinding.btnAction!!.visibility = View.VISIBLE
                        intentData = barcodes.valueAt(0).displayValue
                        mBinding.tvCode.setText(intentData)
                    }
                }
            }

        })
    }

    override fun onPause() {
        super.onPause()
        cameraSource!!.release()
    }

    override fun onResume() {
        super.onResume()
        inicBc()
    }

}