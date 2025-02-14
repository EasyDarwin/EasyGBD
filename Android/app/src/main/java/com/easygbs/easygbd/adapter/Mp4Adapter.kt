import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.easygbs.easygbd.R
import java.io.File

class Mp4Adapter(
    private val context: Context,
    private val mp4Files: List<File>,
    private val onItemClick: (File) -> Unit
) : RecyclerView.Adapter<Mp4Adapter.Mp4ViewHolder>() {

    class Mp4ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val thumbnail: ImageView = view.findViewById(R.id.thumbnail)
//        val fileName: TextView = view.findViewById(R.id.fileName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Mp4ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_mp4, parent, false)
        return Mp4ViewHolder(view)
    }

    override fun onBindViewHolder(holder: Mp4ViewHolder, position: Int) {
        val file = mp4Files[position]
//        holder.fileName.text = file.name

        // 使用 ThumbnailUtils 生成缩略图
        val thumbnailBitmap = android.media.ThumbnailUtils.createVideoThumbnail(
            file.absolutePath,
            android.provider.MediaStore.Video.Thumbnails.MINI_KIND
        )
        holder.thumbnail.setImageBitmap(thumbnailBitmap)

        holder.itemView.setOnClickListener {
            onItemClick(file) // 点击事件回调
        }
    }

    override fun getItemCount(): Int = mp4Files.size
}
