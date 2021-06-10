package com.zxw.screenshot_master

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import java.io.File

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
    }

    var permissions = Manifest.permission.READ_EXTERNAL_STORAGE
    var permissionArray = arrayOf(permissions)
    var imageView: ImageView? = null
    lateinit var viewModel: ScreenShotViewModel

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //获取viewmodel
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(this.application)
        ).get(ScreenShotViewModel::class.java)
        //注册监听者
        viewModel.registerContentObserver()

        //申请权限
        val checkPermission = this.let { ActivityCompat.checkSelfPermission(it, permissions) }
        if (checkPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(permissionArray, 0)
        }

        //数据显示的view承载
        imageView = findViewById(R.id.image)
        //获取图片信息监听 展示图片
        viewModel.screentShotInfoData.observe(this, Observer { data ->
            Toast.makeText(this, "截屏成功了", Toast.LENGTH_LONG).show()
            imageView?.setImageURI(Uri.fromFile(File(data.path)))
        })

        //接收到截屏通知后 主动获取截屏照片
        viewModel.dataChanged.observe(this, Observer {
            viewModel.getLatestImage(null)
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            0 ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "权限申请成功", Toast.LENGTH_LONG).show()
                }
        }
    }
}