package io.github.wagnerarcieri.memorygametemplate

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import io.github.wagnerarcieri.memorygametemplate.model.BoardSize
import kotlin.math.min

class ImagePickerAdapter(
    private val context: Context,
    private val imageURIs: List<Uri>,
    private val boardSize: BoardSize,
    private val imageClickListener: ImageClickListener

) : RecyclerView.Adapter<ImagePickerAdapter.ViewHolder>() {

    interface ImageClickListener {
        fun onPlaceHolderClicked()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivCustomImage = itemView.findViewById<ImageView>(R.id.ivCustomImage)

        fun bind(uri: Uri) {
            ivCustomImage.setImageURI(uri)
            ivCustomImage.setOnClickListener(null)
        }
        fun bind() {
            ivCustomImage.setOnClickListener{
                imageClickListener.onPlaceHolderClicked()
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.card_image, parent, false)
        val cardWidth = parent.width / boardSize.getWidth()
        val cardHeight = parent.height / boardSize.getHeight()
        val cardSideLenght = min(cardHeight, cardWidth)
        val layoutParams = view.findViewById<ImageView>(R.id.ivCustomImage).layoutParams
        layoutParams.width =  cardSideLenght
        layoutParams.height = cardSideLenght
        return ViewHolder(view)
    }

    override fun getItemCount() = boardSize.getNumPairs()


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < imageURIs.size) {
            holder.bind(imageURIs[position])
        } else {
            holder.bind()
        }
    }


}
