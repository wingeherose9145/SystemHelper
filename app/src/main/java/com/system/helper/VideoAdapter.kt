package com.system.helper

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class VideoAdapter(
    context: Context,
    private val items: List<VideoItem>
) : ArrayAdapter<VideoItem>(
    context,
    0,
    items
) {

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {

        val view =
            convertView ?: LayoutInflater
                .from(context)
                .inflate(
                    R.layout.item_video,
                    parent,
                    false
                )

        val item = items[position]

        val thumbImage =
            view.findViewById<ImageView>(
                R.id.thumbImage
            )

        val videoName =
            view.findViewById<TextView>(
                R.id.videoName
            )

        videoName.text =
            item.displayName

        try {

            val retriever =
                MediaMetadataRetriever()

            retriever.setDataSource(
                context,
                item.uri
            )

            val bitmap: Bitmap? =
                retriever.getFrameAtTime(
                    1000000
                )

            if (bitmap != null) {

                thumbImage.setImageBitmap(bitmap)
            }

            retriever.release()

        } catch (_: Exception) {
        }

        return view
    }
}
