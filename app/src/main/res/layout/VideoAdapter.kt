package com.system.helper

import android.content.Context
import android.media.ThumbnailUtils
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class VideoAdapter(
    context: Context,
    private val items: List<VideoItem>
) : ArrayAdapter<VideoItem>(context, 0, items) {

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {

        val view = convertView ?: LayoutInflater
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

        videoName.text = item.displayName

        val bitmap = ThumbnailUtils.createVideoThumbnail(
            item.realFileName,
            MediaStore.Images.Thumbnails.MINI_KIND
        )

        if (bitmap != null) {
            thumbImage.setImageBitmap(bitmap)
        }

        return view
    }
}
