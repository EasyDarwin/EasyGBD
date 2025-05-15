import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.easygbs.easygbd.R
import java.io.File

class Mp4PagerAdapter(
    private val context: Context,
    private val mp4Files: List<File>,
    private val onItemClick: (File) -> Unit
) : RecyclerView.Adapter<Mp4PagerAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnail: ImageView = itemView.findViewById(R.id.videoThumbnail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.item_video_thumbnail, parent, false)
        val layoutParams = view.layoutParams
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        view.layoutParams = layoutParams
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = mp4Files[position]
        // 使用 Glide 加载视频缩略图
        Glide.with(context)
            .load(file)
            .placeholder(R.mipmap.ic_video_placeholder)
            .into(holder.thumbnail)

        // 点击事件
        holder.itemView.setOnClickListener {
            onItemClick(file)
        }
    }

    override fun getItemCount(): Int = mp4Files.size
}
