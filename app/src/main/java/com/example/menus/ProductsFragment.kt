package com.example.menus

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.menus.databinding.FragmentProductsBinding
import com.example.menus.databinding.ItemProductBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import java.text.NumberFormat


class ProductsFragment : Fragment() {

    private var requestCamera: ActivityResultLauncher<String>? = null

    private lateinit var mBinding: FragmentProductsBinding
    private lateinit var fab: FloatingActionButton

    private lateinit var mFirebaseAdapter: FirebaseRecyclerAdapter<Product, ProductHolder>
    private lateinit var mLayoutManager: RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        mBinding =  FragmentProductsBinding.inflate(inflater, container, false)

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val query = FirebaseDatabase.getInstance().reference.child("products")
        val options = FirebaseRecyclerOptions.Builder<Product>().setQuery(query, Product::class.java).build()

        mFirebaseAdapter = object : FirebaseRecyclerAdapter<Product, ProductHolder>(options){

            private lateinit var mContext: Context

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductHolder {
                mContext = parent.context
                val view = LayoutInflater.from(mContext).inflate(R.layout.item_product, parent, false)
                return ProductHolder(view)
            }

            override fun onBindViewHolder(holder: ProductHolder, position: Int, model: Product) {
                val product = getItem(position)

                with(holder){
                    setListener(product)

                    //binding.tvCodeProduct.text = product.id
                    binding.tvTitleProduct.text = product.nombre
                    binding.tvCategoryProduct.text = product.categoria
                    binding.tvCostProduct.text = "$ ${formatPrice(product.pVenta)}"

                        Glide.with(mContext)
                            .load(product.urlImage)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(binding.ivProduct)
                }
            }

            override fun onDataChanged() {
                super.onDataChanged()
                mBinding.progressBar.visibility = View.GONE
                notifyDataSetChanged()
            }

            override fun onError(error: DatabaseError) {
                super.onError(error)
                Toast.makeText(mContext, error.message, Toast.LENGTH_LONG).show()
            }

        }
        mLayoutManager = LinearLayoutManager(context)

        mBinding.reciclerView.apply {
            setHasFixedSize(true)
            layoutManager = mLayoutManager
            adapter = mFirebaseAdapter

        }

        requestCamera = registerForActivityResult(ActivityResultContracts.RequestPermission()){
            if(it){
                val intent = Intent( requireContext(), FindProductsByBarcodeScan::class.java)
                startActivity(intent)
            }else{
                Toast.makeText(requireContext(),"Permission Not Granted", Toast.LENGTH_LONG).show()
            }
        }

        fab = mBinding.fab

        fab.setOnClickListener {
            showBottomDialog()
        }
    }

    private fun showBottomDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottomsheetlayout)

        val createLayout = dialog.findViewById<LinearLayout>(R.id.layoutCreateProduct)
        val readLayout = dialog.findViewById<LinearLayout>(R.id.layoutBarCode)
        val cancelButton = dialog.findViewById<ImageView>(R.id.cancelButton)

        createLayout.setOnClickListener {
            val fragmentManager = childFragmentManager
            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragment_container_products, EditProductFragment())
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()

            //hide the fab
            fab.visibility = View.GONE

            dialog.dismiss()
        }

        readLayout.setOnClickListener {

            requestCamera?.launch(android.Manifest.permission.CAMERA)

            //hide the fab
            fab.visibility = View.GONE
            dialog.dismiss()
        }

        cancelButton.setOnClickListener { dialog.dismiss() }

        dialog.show()

        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
    }

    override fun onResume() {
        super.onResume()
        fab.visibility = View.VISIBLE
    }


    override fun onStart() {
        super.onStart()
        mFirebaseAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        mFirebaseAdapter.stopListening()
    }

    private fun formatPrice(price: Double): String{
        val formatoNumero: NumberFormat = NumberFormat.getNumberInstance()
        formatoNumero.setMaximumFractionDigits(0)
        return formatoNumero.format(price).toString()
    }

    // Function to show the dialog
    private fun showAlertDialogEdit(product: Product) {
        activity?.let {
            val items = arrayOf("Editar Producto", "Eliminar Producto", "Cancelar")
            MaterialAlertDialogBuilder(it)
                .setTitle("${product.nombre}")
                .setItems(items) { _, i ->
                    when(i){
                        0 -> update(product)
                        1 -> delete(product)
                        2 -> null
                    }
                }
                .show()

        }
    }

    private fun update(product: Product) {
        // Fragmento de origen
        val bundle = Bundle().apply {
            putString("id", product.id)
            putString("urlImage", product.urlImage)
            putString("codBarras", product.codBarras)
            putString("nombre", product.nombre)
            putString("categoria", product.categoria)
            putDouble("pCompra", product.pCompra)
            putDouble("porc_ganancia", product.porc_ganancia)
            putDouble("pVenta", product.pVenta)
        }

        val fragmentoDestino = EditProductFragment()
        fragmentoDestino.arguments = bundle

        val fragmentManager = childFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container_products, fragmentoDestino)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()

        //hide the fab
        fab.visibility = View.GONE
    }

    private fun delete(product: Product) {
        activity?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle("Eliminar Producto")
                .setMessage("¿Eliminar ${product.nombre}?")
                .setPositiveButton("Aceptar") { _, _ ->
                    val productReference = FirebaseDatabase.getInstance().reference.child("products").child(product.id)
                    productReference.removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Producto Eliminado Correctamente", Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Ha Ocurrido Un Error!", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("Cancelar") { _, _ ->
                    //code here
                }
                .show()

        }
    }

    inner class ProductHolder(view: View) : RecyclerView.ViewHolder(view){
        val binding = ItemProductBinding.bind(view)


        fun setListener(product: Product){
                binding.cvItemContainer.setOnLongClickListener {
                    //Toast.makeText(requireContext(), product.nombre.toString(), Toast.LENGTH_SHORT).show()
                    showAlertDialogEdit(product)
                    false
                }
        }
    }

}