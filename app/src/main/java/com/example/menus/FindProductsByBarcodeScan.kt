package com.example.menus

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.menus.databinding.ActivityBarcodeScanBinding
import com.example.menus.databinding.ActivityFindProductsByBarcodeScanBinding
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.IOException
import java.text.NumberFormat

class FindProductsByBarcodeScan : AppCompatActivity() {

    private lateinit var mBinding: ActivityFindProductsByBarcodeScanBinding
    private lateinit var barCodeDetector: BarcodeDetector
    private lateinit var  cameraSource: CameraSource

    private var database: DatabaseReference = FirebaseDatabase.getInstance().getReference()

    var intentData = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityFindProductsByBarcodeScanBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mBinding.btnAction.setOnClickListener {

            mBinding.cvContainer.visibility = View.INVISIBLE

            mBinding.progressBar.visibility = View.VISIBLE
            database.child("products")
                .orderByChild("codBarras").equalTo(mBinding.tvCode.text.toString().trim()).get()
                .addOnSuccessListener {result ->
                    for (product in result.children){
                        mBinding.tvTitleProduct.text = product.child("nombre").getValue().toString()
                        mBinding.tvCategoryProduct.text = product.child("categoria").getValue().toString()
                        mBinding.tvCostProduct.text = "$ ${formatPrice(product.child("pventa").getValue().toString().toDouble())}"

                        Glide.with(this)
                            .load(product.child("urlImage").getValue().toString())
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(mBinding.ivProduct)

                        mBinding.cvContainer.visibility = View.VISIBLE
                    }
                    mBinding.progressBar.visibility = View.INVISIBLE
                    if(mBinding.cvContainer.visibility == View.INVISIBLE){
                        Toast.makeText(this, "Producto No Encontrado", Toast.LENGTH_LONG).show()
                    }

                }
                .addOnFailureListener {
                    mBinding.progressBar.visibility = View.INVISIBLE
                    Toast.makeText(this, "Ha Ocurrido Un Error!", Toast.LENGTH_LONG).show()
                }

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

    private fun formatPrice(price: Double): String{
        val formatoNumero: NumberFormat = NumberFormat.getNumberInstance()
        formatoNumero.setMaximumFractionDigits(0)
        return formatoNumero.format(price).toString()
    }
}