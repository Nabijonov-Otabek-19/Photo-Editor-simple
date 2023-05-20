package uz.gita.myphotoeditor_bek

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PointF
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import uz.gita.myphotoeditor_bek.data.AddViewData
import uz.gita.myphotoeditor_bek.databinding.ActivityMainBinding
import uz.gita.myphotoeditor_bek.databinding.ContainerViewBinding
import uz.gita.myphotoeditor_bek.utils.lineLength
import uz.gita.myphotoeditor_bek.utils.px

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var addViewData: AddViewData? = null
    private var lastSelectView: ContainerViewBinding? = null

    private val RESULT_LOAD_IMAGE = 1
    private val CAMERA_REQUEST = 1888

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            addGlasses.setOnClickListener {
                addViewData = AddViewData.EmojiData(R.drawable.glasses, 60.px, 30.px)
            }

            addText.setOnClickListener {
                //
            }

            addImage.setOnClickListener {
                pickImageFromGallery()
            }

            addFromCamera.setOnClickListener {
                pickImageFromCamera()
            }
        }

        binding.editor.setOnTouchListener { _, event ->
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
    }

    private fun pickImageFromCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST)
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

        // from camera
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            val photo: Bitmap = data?.extras?.get("data") as Bitmap
            binding.mainImage.setImageBitmap(photo)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addView(targetX: Float, targetY: Float) {

        val _view: View = when (addViewData!!) {
            is AddViewData.EmojiData -> {
                ImageView(this).apply {
                    setImageResource((addViewData as AddViewData.EmojiData).imageResID)
                }
            }

            is AddViewData.TextData -> {
                TextView(this).apply { }
            }
        }

        val containerBinding = ContainerViewBinding.inflate(layoutInflater, binding.editor, false)

        containerBinding.root.x = targetX - 50.px
        containerBinding.root.y = targetY - 30.px

        containerBinding.viewContainer.addView(_view)
        binding.editor.addView(containerBinding.root, 100.px, 60.px)
        selectView(containerBinding)

        containerBinding.buttonCancel.setOnClickListener {
            binding.editor.removeView(containerBinding.root)
        }

        var lastPoint = PointF()
        var firstPoint = PointF()
        var secondPoint: PointF? = null
        var oldLength = 0.0

        containerBinding.root.setOnTouchListener { _, event ->
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

                        //containerBinding.root.rotation += alpha
                    }
                }
            }
            return@setOnTouchListener true
        }
    }

    private fun selectView(view: ContainerViewBinding) {
        if (lastSelectView != view) unSelect()
        lastSelectView = view
        lastSelectView!!.apply {
            this.viewContainer.isSelected = true
            this.buttonCancel.visibility = View.VISIBLE
        }
    }

    private fun unSelect() {
        lastSelectView?.let {
            it.viewContainer.isSelected = false
            it.buttonCancel.visibility = View.GONE
        }
    }
}