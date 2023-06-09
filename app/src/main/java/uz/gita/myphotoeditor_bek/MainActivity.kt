package uz.gita.myphotoeditor_bek

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PointF
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout.LayoutParams
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import uz.gita.myphotoeditor_bek.adapter.TextColorAdapter
import uz.gita.myphotoeditor_bek.data.AddViewData
import uz.gita.myphotoeditor_bek.databinding.ActivityMainBinding
import uz.gita.myphotoeditor_bek.databinding.ContainerBinding
import uz.gita.myphotoeditor_bek.utils.Constants
import uz.gita.myphotoeditor_bek.utils.lineLength
import uz.gita.myphotoeditor_bek.utils.px
import uz.gita.myphotoeditor_bek.utils.toast
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class MainActivity : AppCompatActivity(), MyRotationGestureDetector.OnRotationGestureListener {

    private lateinit var binding: ActivityMainBinding
    private var addViewData: AddViewData? = null
    private var lastSelectView: ContainerBinding? = null

    private val adapter by lazy { TextColorAdapter() }

    private val mRotationDetector: MyRotationGestureDetector by lazy {
        MyRotationGestureDetector(
            this@MainActivity
        )
    }

    private val RESULT_LOAD_IMAGE = 1

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                val bitmap = getBitmapFromView(binding.editor)
                saveMediaToStorage(bitmap)
            }
        }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val storagePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        binding.apply {
            addGlasses.setOnClickListener {
                addViewData = AddViewData.EmojiData(R.drawable.mask, 100.px, 30.px)
            }

            addText.setOnClickListener {
                addViewData = AddViewData.TextData("Hello world", 16f, Color.BLACK)
            }

            addImage.setOnClickListener {
                pickImageFromGallery()
            }

            btnSave.setOnClickListener {
                if (storagePermission) {
                    val bitmap = getBitmapFromView(binding.editor)
                    saveMediaToStorage(bitmap)
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }

            editor.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    when (addViewData) {
                        is AddViewData.EmojiData -> {
                            addView(event.x, event.y)
                        }

                        is AddViewData.TextData -> {
                            addView(event.x, event.y)
                        }

                        null -> {
                            unSelect()
                        }
                    }
                    addViewData = null
                }
                return@setOnTouchListener true
            }

            btnTextColor.setOnClickListener {
                setRecyclerItems()
            }

            binding.edtText.doAfterTextChanged { text ->
                val childView = lastSelectView!!.viewContainer.getChildAt(0)
                if (childView is TextView) {
                    childView.text = text
                }
            }
        }

        adapter.setTouchListener {
            val childView = lastSelectView!!.viewContainer.getChildAt(0)
            if (childView is TextView) {
                childView.setTextColor(ContextCompat.getColor(this, it))
            }
        }
    }

    private fun setRecyclerItems() {
        binding.apply {
            recyclerItems.visibility = View.VISIBLE
            adapter.setData(Constants.colorList)
            recyclerItems.adapter = adapter
            recyclerItems.layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    // Get bitmap of view
    private fun getBitmapFromView(view: View): Bitmap {
        val img = binding.mainImage
        val bitmap = Bitmap.createBitmap(
            img.width, img.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    // Function to save an Image
    private fun saveMediaToStorage(bitmap: Bitmap) {
        val filename = "${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }
        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            toast("Saved to Gallery")
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, RESULT_LOAD_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // from gallery
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            val selectedImage: Uri? = data.data
            binding.mainImage.setImageURI(selectedImage)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addView(targetX: Float, targetY: Float) {

        val _view: View = when (addViewData!!) {
            is AddViewData.EmojiData -> {
                ImageView(this).apply {
                    val wid = (addViewData as AddViewData.EmojiData).defWidth
                    val hei = (addViewData as AddViewData.EmojiData).defHeight
                    layoutParams = ViewGroup.LayoutParams(wid, hei)
                    setImageResource((addViewData as AddViewData.EmojiData).imageResID)
                }
            }

            is AddViewData.TextData -> {
                TextView(this).apply {
                    text = (addViewData as AddViewData.TextData).st
                    gravity = Gravity.CENTER
                    textSize = (addViewData as AddViewData.TextData).defTextSize
                    setTextColor((addViewData as AddViewData.TextData).defColor)
                }
            }
        }

        val containerBinding = ContainerBinding.inflate(layoutInflater, binding.editor, false)

        containerBinding.root.x = targetX - 50.px
        containerBinding.root.y = targetY - 30.px

        val layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )

        containerBinding.viewContainer.addView(_view)
        binding.editor.addView(containerBinding.root, layoutParams)
        selectView(containerBinding)

        // For saving edited photo
        binding.btnSave.visibility = View.VISIBLE

        containerBinding.buttonCancel.setOnClickListener {
            binding.editor.removeView(containerBinding.root)
            binding.textContainer.visibility = View.GONE
            binding.recyclerItems.visibility = View.GONE
        }

        var lastPoint = PointF()
        var firstPoint = PointF()
        var secondPoint: PointF? = null
        var oldLength = 0.0

        containerBinding.root.setOnTouchListener { _, event ->

            mRotationDetector.onTouchEvent(event)

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    selectView(containerBinding)
                    lastPoint = PointF(event.x, event.y)
                    firstPoint = PointF(event.x, event.y)
                }

                MotionEvent.ACTION_MOVE -> {
                    if (event.pointerCount == 1) {
                        val distanceX = event.x - lastPoint.x
                        val distanceY = event.y - lastPoint.y

                        containerBinding.root.x += distanceX
                        containerBinding.root.y += distanceY
                    } else {
                        val index0 = event.findPointerIndex(0)
                        val index1 = event.findPointerIndex(1)

                        val x0 = event.getX(index0)
                        val y0 = event.getY(index0)

                        val x1 = event.getX(index1)
                        val y1 = event.getY(index1)

                        if (secondPoint == null) {
                            secondPoint = PointF(x1, y1)
                            oldLength = lineLength(firstPoint, secondPoint!!)
                        }

                        val newLength = lineLength(PointF(x0, y0), PointF(x1, y1))
                        val k = newLength / oldLength

                        if (containerBinding.root.scaleX * k > 0.1) {
                            containerBinding.root.scaleX *= k.toFloat()
                            containerBinding.root.scaleY *= k.toFloat()
                        }
                    }
                }
            }
            return@setOnTouchListener true
        }
    }

    private fun selectView(view: ContainerBinding) {
        if (lastSelectView != view) unSelect()
        lastSelectView = view
        lastSelectView!!.apply {
            this.viewContainer.isSelected = true
            this.buttonCancel.visibility = View.VISIBLE
        }

        val childView = lastSelectView?.viewContainer?.getChildAt(0)
        if (childView is TextView) {
            binding.textContainer.visibility = View.VISIBLE
            binding.edtText.hint = "Type something"
        }
    }

    private fun unSelect() {
        binding.recyclerItems.visibility = View.GONE
        binding.textContainer.visibility = View.GONE

        lastSelectView?.let {
            it.viewContainer.isSelected = false
            it.buttonCancel.visibility = View.GONE
        }
    }

    override fun onRotation(rotationDetector: MyRotationGestureDetector?) {
        lastSelectView!!.root.apply {
            if (rotationDetector != null) {
                rotation += rotationDetector.angle
                val cS = scaleX
                scaleX = rotationDetector.scale * cS
                scaleY = rotationDetector.scale * cS
            }
        }
    }
}