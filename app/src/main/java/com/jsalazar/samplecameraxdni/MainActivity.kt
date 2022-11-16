package com.jsalazar.samplecameraxdni

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.jsalazar.cameraxdni.camerax.CameraUtil
import com.jsalazar.cameraxdni.camerax.Selector
import com.jsalazar.cameraxdni.utils.CameraTimer
import com.jsalazar.samplecameraxdni.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val vm by viewModels<MainViewModel>()
    private lateinit var binding: ActivityMainBinding

    private val cameraUtil: CameraUtil by lazy {
        CameraUtil(this)
            .registerLifecycle(lifecycle)
            .setLifecycleOwner(this)
            .setCoroutineScope(this.lifecycleScope)
            .setPreviewView(binding.viewFinder)
            .setTimer(CameraTimer.S3)
            .setImageQuality(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setFlashMode(ImageCapture.FLASH_MODE_OFF)
            .setCameraSelector(CameraSelector.DEFAULT_BACK_CAMERA)
            .setEnableTorch(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startCamera()

        binding.cameraCaptureButton.setOnClickListener {
            /*cameraUtil.takePicture(outputDirectory = outputDirectory(), {
                val msg = "Photo capture succeeded: $it"
                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                Log.d(CameraUtil.TAG, msg)
            }, {
                vm.setTimer(it)
            })*/
            /*  cameraUtil.takeSnapshot(outputDirectory = outputDirectory(), fileName = System.currentTimeMillis().toString()) {
                  val msg = "Photo capture succeeded: $it"
                  Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                  Log.d(CameraUtil.TAG, msg)
              }*/
            /*cameraUtil.takeSnapshotGallery(path = "/Pictures/" + resources.getString(R.string.app_name),
                fileName = System.currentTimeMillis().toString()) {
                val msg = "Photo capture succeeded: $it"
                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                Log.d(CameraUtil.TAG, msg)
            }*/
            cameraUtil.takePhotoCenterCrop(480, 720) { bitMap ->
                binding.imgBit.setImageBitmap(bitMap)
            }
        }

        binding.btnTorch.setOnClickListener {
            /*cameraUtil.flash(
                if (cameraUtil.getFlashMode() == ImageCapture.FLASH_MODE_OFF) {
                    Toast.makeText(this, "FLASH_MODE_ON", Toast.LENGTH_SHORT).show()
                    ImageCapture.FLASH_MODE_ON
                } else {
                    Toast.makeText(this, "FLASH_MODE_OFF", Toast.LENGTH_SHORT).show()
                    ImageCapture.FLASH_MODE_OFF
                }
            )*/
            cameraUtil.setEnableTorch(!cameraUtil.isTorchEnable())
            if (cameraUtil.isTorchEnable()){
                binding.btnTorch.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_flash))
            }else{
                binding.btnTorch.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_flash_off))
            }
        }

        binding.btnMirror.setOnClickListener {
            cameraUtil.flip { selector ->
                vm.setCameraSelector(selector)
            }
        }

        observeLiveData()
    }

    /*private fun outputDirectory(): String {
        val mediaDir = this.externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply {
                mkdirs()
            }
        }

        return if (mediaDir != null && mediaDir.exists()) mediaDir.absolutePath else filesDir.absolutePath
    }*/

    private fun startCamera() {
        if (allPermissionsGranted()) {
            try {
                cameraUtil.startCamera()
            } catch (exception: IllegalStateException) {
                Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
            } catch (exception: Exception) {
                Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
            }
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    private fun observeLiveData() {
        vm.cameraSelector.observe(this) {
            when (it) {
                Selector.FRONT -> {
                    binding.identityFrame.visibility = View.GONE
                    binding.personFrame.visibility = View.VISIBLE
                }

                Selector.BACK -> {
                    binding.identityFrame.visibility = View.VISIBLE
                    binding.personFrame.visibility = View.GONE
                }

                else -> {
                    binding.identityFrame.visibility = View.VISIBLE
                    binding.personFrame.visibility = View.GONE
                }
            }

        }

        vm.timer.observe(this) {
            binding.tvTimer.visibility = if (it > 0) View.VISIBLE else View.GONE
            if (it > 0) {
                binding.tvTimer.text = String.format("Timer: %s",it)
            }
        }
    }


    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                cameraUtil.startCamera()
            } else {
                Toast.makeText(this, "Permission not granted by the user.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}