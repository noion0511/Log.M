package com.likewhile.meme.ui.view

import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.net.Uri
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.likewhile.meme.BuildConfig
import com.likewhile.meme.R
import com.likewhile.meme.databinding.ActivityImageMemoEditBinding
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class ImageMemoEditActivity : AppCompatActivity() {
    private lateinit var filePath : String
    private lateinit var fileUri : Uri
    private var imeageSettingMode : String = "uri"
    private lateinit var bitmap : Bitmap

    private val binding : ActivityImageMemoEditBinding by lazy {
        ActivityImageMemoEditBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initMemoData()
        initToolbar()
        initCreateBtn()
    }

    private fun setReadMode(){

    }
    private fun setWriteMode(){

    }
    private fun initMemoData() {

    }

    private fun initToolbar() {
        val params = Toolbar.LayoutParams(
            Toolbar.LayoutParams.MATCH_PARENT,
            Toolbar.LayoutParams.WRAP_CONTENT,
            Gravity.START
        )
        setSupportActionBar(binding.toolbar.toolbar)
        val blackColor = ContextCompat.getColor(this, android.R.color.black)
        binding.toolbar.logo.setColorFilter(blackColor, PorterDuff.Mode.SRC_IN)
        binding.toolbar.toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_24)
        binding.toolbar.toolbar.layoutParams = params
        binding.toolbar.toolbar.setNavigationOnClickListener { finish() }
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }
    private fun initCreateBtn(){
        binding.imageAddButton.imageAddButton.setOnClickListener {
            showDialog()
        }
        binding.imageContent.imageView.setOnClickListener {
            showDialog()
        }
        binding.bottomBtnEdit.buttonSave.setOnClickListener {
            if(imeageSettingMode=="bitmap"){
                if(makePathName()){
                    saveImageInLocalStorage()
                }else{
                    Toast.makeText(this, "이미지 저장에 실패했습니다", Toast.LENGTH_SHORT).show()
                }
            }else{

            }
        }
    }

    private fun showDialog(){
        val navigationOptions = arrayOf(
            "사진 촬영",
            "갤러리에서 사진 선택"
        )

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.choose_a_screen))
            .setItems(navigationOptions) { _, which ->
                val intent = when (navigationOptions[which]) {
                    "사진 촬영" -> {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        requestCameraImageLauncher.launch(intent)
                    }
                    "갤러리에서 사진 선택" ->{
                        requestGalleryImageLauncher.launch("image/*")
                    }
                    else -> null
                }
            }
        builder.create().show()
    }

    private fun setImageView() {//이미지 뷰에 사진을 세팅합니다
        if(imeageSettingMode=="uri"){
            binding.imageContent.imageView.setImageURI(fileUri)
        }else{
            binding.imageContent.imageView.setImageBitmap(bitmap)
        }
        binding.imageAddButton.root.visibility = View.GONE
        binding.imageContent.root.visibility = View.VISIBLE
    }

    private fun saveImageInLocalStorage(){//촬영한 이미지를 external storage에 저장합니다
        val imageFile= File(filePath)
        try {
            imageFile.createNewFile()
            val out =FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.close()
            fileUri=FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID+".fileprovider", imageFile)
        } catch (e:java.lang.Exception) {
            e.printStackTrace()
            Toast.makeText(getApplicationContext(), "이미지 저장 실패", Toast.LENGTH_SHORT).show();
        }
    }

    private fun makePathName() : Boolean{//촬영한 이미지를 저장할 경로를 만듭니다
        val uuid = UUID.randomUUID()
        val externalFileDir : String? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath
        if(externalFileDir!=null){
            val cal = Calendar.getInstance()
            val dateString = cal.get(Calendar.YEAR).toString()+
                    cal.get(Calendar.MONTH+1).toString()+
                    cal.get(Calendar.DATE).toString()+
                    cal.get(Calendar.HOUR).toString()+
                    cal.get(Calendar.MINUTE).toString()+
                    cal.get(Calendar.SECOND).toString()+
                    uuid.toString()
            filePath=externalFileDir+"/$dateString"+".png"
            return true
        }else{
            return false
        }
    }

    private val requestCameraImageLauncher=registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode== RESULT_OK) {
            bitmap= it.data?.extras?.get("data") as Bitmap
            imeageSettingMode="bitmap"
            setImageView()
        }
    }

    private val requestGalleryImageLauncher=registerForActivityResult(
        ActivityResultContracts.GetContent()
    ){
        if(it!=null){
            fileUri=it
            imeageSettingMode="uri"
            setImageView()
        }
    }

}