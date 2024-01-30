package com.example.menus

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.menus.databinding.FragmentEditProductBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class EditProductFragment : Fragment() {

    private var barCode: String? = null
    private var idEdit: String? = null
    private var codBarras: String? = null
    private var nombre: String? = null
    private var categoria: String? = null
    private var urlImage: String? = null
    private var pCompra: Double? = null
    private var pVenta: Double? = null
    private var porc_ganancia: Double? = null

    private lateinit var mBinding: FragmentEditProductBinding

    private var requestCameraPermission : ActivityResultLauncher<String>? = null
    private var requestCamera : ActivityResultLauncher<Intent>? = null

    private var database: DatabaseReference = FirebaseDatabase.getInstance().getReference("products")

    // Utiliza push() para generar un nuevo ProductId y obtener la referencia a esa ubicación
    private var newProductRef: DatabaseReference = database.push()

    // Obtén el nuevo productId generado automáticamente
    val productId: String = newProductRef.key.toString()

    private lateinit var selectedCategory : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            barCode = it.getString("barCode")

            idEdit = it.getString("id")
            codBarras = it.getString("codBarras")
            nombre = it.getString("nombre")
            categoria = it.getString("categoria")
            urlImage = it.getString("urlImage")
            pCompra = it.getDouble("pCompra")
            pVenta = it.getDouble("pVenta")
            porc_ganancia = it.getDouble("porc_ganancia")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        mBinding = FragmentEditProductBinding.inflate(inflater, container, false)
        return mBinding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // botón atrás
        onBackPressed()
        // Dropdown config
        setUpDropdownCategories()

        //EditMode
        if(idEdit != null){
            fillFilesInEditMode()
        }

        requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){
            if(it){
                startBarCodeScanActivity()
            }else{
                Toast.makeText(requireContext(),"Permission Not Granted", Toast.LENGTH_LONG).show()
            }
        }

        requestCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // El código de barras escaneado se encuentra en result.data
                val barCode = result.data?.getStringExtra("barCode")
                // Haz algo con el código de barras, por ejemplo, actualiza el formulario en tu fragmento
                updateBarCode(barCode)
            } else {
                // Manejar el caso en que la actividad no fue completada con éxito
                Toast.makeText(requireContext(), "Error al escanear el código de barras", Toast.LENGTH_LONG).show()
            }
        }

        mBinding.btnBarCode.setOnClickListener {
            requestCameraPermission?.launch(android.Manifest.permission.CAMERA)
        }

        mBinding.etUrlImagen.addTextChangedListener{
            if(!it.toString().trim().isNullOrEmpty()){
                loadImage(it.toString().trim())
            }else{
                mBinding.imgProduct.setImageResource(R.drawable.ic_image)
            }
        }

        mBinding.etPrecioCompra.addTextChangedListener {
            if(!it.isNullOrEmpty()){
                calculateSellPriceByBuyPrice(it.toString().trim().toDouble(), mBinding.etPorcGanancia.text.toString().trim())
            }
        }

        mBinding.etPorcGanancia.addTextChangedListener {
            if (!it.isNullOrEmpty()){
                calculateSellPriceByPercent(it.toString().trim().toDouble(), mBinding.etPrecioCompra.text.toString().trim())
            }
        }

        mBinding.btnSend.setOnClickListener {
            val pCompra = mBinding.etPrecioCompra.text.toString().trim().toDouble()
            val pVenta = mBinding.etPrecioVenta.text.toString().trim().toDouble()
            val percent = (((pVenta/pCompra) - 1) * 100).toString().substringBefore(".")


            val product = Product(
                id = idEdit ?: productId,
                urlImage = mBinding.etUrlImagen.text.toString().trim(),
                codBarras = mBinding.etCodBarras.text.toString().trim(),
                nombre = mBinding.etNombre.text.toString().trim(),
                categoria = selectedCategory,
                pCompra = mBinding.etPrecioCompra.text.toString().trim().toDouble(),
                porc_ganancia = percent.toDouble(),
                pVenta = mBinding.etPrecioVenta.text.toString().trim().toDouble(),
            )

            if (idEdit != null){
                //update
                val productMap = product.toMap()
                database.child(idEdit!!).updateChildren(productMap).addOnSuccessListener {
                    val fragmentManager = requireActivity().supportFragmentManager
                    clearFields()
                    fragmentManager.beginTransaction().replace(R.id.fragment_container_products, ProductsFragment()).commit()
                    Toast.makeText(requireContext(), "Producto Actualizado Exitosamente", Toast.LENGTH_LONG).show()
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Ha Ocurrido Un Error", Toast.LENGTH_LONG).show()
                }
            }else{
                //create
                newProductRef.setValue(product).addOnSuccessListener {
                    val fragmentManager = requireActivity().supportFragmentManager
                    clearFields()
                    fragmentManager.beginTransaction().replace(R.id.fragment_container_products, ProductsFragment()).commit()
                    Toast.makeText(requireContext(), "Producto Agregado Exitosamente", Toast.LENGTH_LONG).show()
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Ha Ocurrido Un Error", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    private fun updateBarCode(barCode: String?) {
        mBinding.etCodBarras.setText(barCode)
    }

    private fun startBarCodeScanActivity() {
       val intent = Intent(requireContext(), BarcodeScan::class.java)
        requestCamera?.launch(intent)
    }

    private fun clearFields() {
        mBinding.etUrlImagen.setText("")
        mBinding.etCodBarras.setText("")
        mBinding.etNombre.setText("")
        mBinding.etPrecioCompra.setText("")
        mBinding.etPorcGanancia.setText("")
        mBinding.etPrecioVenta.setText("")
        mBinding.spinnerCategoria.setSelection(0)
    }

    private fun calculateSellPriceByPercent(percent: Double, pCompra: String) {
        var pVenta: String
        if (pCompra.isNullOrEmpty()){
            pVenta = "0"
            mBinding.etPrecioVenta.setText(pVenta.substringBefore("."))
            mBinding.etPrecioCompra.setText("0")

        }else{
            pVenta = (pCompra.toDouble() * (1 + percent/100)).toString()
            mBinding.etPrecioVenta.setText(pVenta.substringBefore("."))
        }
    }

    private fun calculateSellPriceByBuyPrice(pCompra: Double, percent: String) {
        var pVenta: String
        if (percent.isNullOrEmpty()){
            //mBinding.spinnerPorcGanancia.setSelection(4)
            pVenta = (pCompra * 1.25).toString()
            mBinding.etPorcGanancia.setText("25")
            mBinding.etPrecioVenta.setText(pVenta.substringBefore("."))

        }else{
            pVenta = (pCompra * (1 + percent.toDouble()/100)).toString()
            mBinding.etPrecioVenta.setText(pVenta.substringBefore("."))
        }
    }

    private fun loadImage(url: String) {
        Glide.with(this)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
            .into(mBinding.imgProduct)
    }

    private fun setUpDropdownCategories() {
        val categories = resources.getStringArray(R.array.categories)

        val adapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val spinner: Spinner = mBinding.spinnerCategoria

        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            )
            {
                if (position != 0){
                    selectedCategory = categories[position]
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }
    }

    private fun onBackPressed() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fragmenManager = requireActivity().supportFragmentManager

                fragmenManager.beginTransaction().replace(R.id.frame_layout, ProductsFragment()).commit()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun fillFilesInEditMode() {

        mBinding.btnSend.text = "Actualizar"

        mBinding.etUrlImagen.setText(urlImage)
        urlImage.let { loadImage(it!!) }

        mBinding.etCodBarras.setText(codBarras)
        mBinding.etNombre.setText(nombre)
        mBinding.etPrecioCompra.setText(pCompra.toString().substringBefore("."))
        mBinding.etPorcGanancia.setText(porc_ganancia.toString().substringBefore("."))
        mBinding.etPrecioVenta.setText(pVenta.toString().substringBefore("."))

        //dropdown edit
        val categories = resources.getStringArray(R.array.categories)

        for (index in 0 until categories.size){
            if(categories[index] == categoria){
                mBinding.spinnerCategoria.setSelection(index)
                return
            }
        }

    }


}