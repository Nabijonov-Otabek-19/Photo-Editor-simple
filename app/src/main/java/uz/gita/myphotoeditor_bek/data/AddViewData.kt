package uz.gita.myphotoeditor_bek.data

sealed interface AddViewData {

    data class EmojiData(
        val imageResID: Int,
        val defWidth: Int,
        val defHeight: Int
    ) : AddViewData

    data class TextData(
        val st: String,
        val defTextSize: Float,
        val defColor: Int
    ) : AddViewData
}