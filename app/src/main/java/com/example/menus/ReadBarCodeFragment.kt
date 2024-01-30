package com.example.menus

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.menus.databinding.FragmentReadBarCodeBinding

class ReadBarCodeFragment : Fragment() {

    private var requestCamera: ActivityResultLauncher<String>? = null

    private lateinit var mBinding: FragmentReadBarCodeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        mBinding = FragmentReadBarCodeBinding.inflate(inflater, container, false)
        return mBinding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // botón atrás
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fragmenManager = requireActivity().supportFragmentManager

                fragmenManager.beginTransaction().replace(R.id.frame_layout, ProductsFragment()).commit()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        requestCamera = registerForActivityResult(ActivityResultContracts
            .RequestPermission()){
            if(it){
                val intent = Intent( requireContext(), FindProductsByBarcodeScan::class.java)
                startActivity(intent)
            }else{
                Toast.makeText(requireContext(),"Permission Not Granted", Toast.LENGTH_LONG).show()
            }
        }

        mBinding.btnScan.setOnClickListener {
            requestCamera?.launch(android.Manifest.permission.CAMERA)
        }
    }


}