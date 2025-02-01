package ru.coursefinder.app.ui.adapters

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import ru.coursefinder.app.R
import ru.coursefinder.app.databinding.CourseItemLayoutBinding
import ru.coursefinder.app.utils.getDisplayableDateString
import ru.coursefinder.app.utils.parseToDate
import ru.coursefinder.domain.model.Course
import java.util.Locale

internal class CoursesAdapter(
    private val listener: CourseItemListener
) : PagingDataAdapter<Course, CoursesAdapter.CourseViewHolder>(DiffUtilCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CourseItemLayoutBinding.inflate(inflater, parent, false)
        return CourseViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }


    class CourseViewHolder(
        private val binding: CourseItemLayoutBinding,
        private val listener: CourseItemListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(course: Course) {
            setupCoverImageView(course)
            binding.bookmarkButton.apply {
                when {
                    course.isFavourite -> setImageResource(R.drawable.icon_bookmark_filled)
                    else -> setImageResource(R.drawable.icon_bookmark)
                }

                setOnClickListener {
                    listener.saveCourse(course)
                }
            }

            binding.rating.apply {
                root.isVisible = course.rating != null
                icon.isVisible = true
                icon.setImageResource(R.drawable.icon_star_filled)
                course.rating?.let {
                    text.text = String.format(Locale.getDefault(), "%.2f", it)
                }
            }

            binding.publishDate.apply {
                text.text = course.publishDate.parseToDate().getDisplayableDateString()
            }

            binding.learnersCount.apply {
                root.isVisible = course.learnersCount?.takeIf { it > 0 } != null
                icon.isVisible = true
                icon.setImageResource(R.drawable.icon_person_filled)
                course.learnersCount?.let {
                    text.text = String.format(Locale.getDefault(), "%d", it)
                }
            }

            binding.title.apply {
                text = when (Locale.getDefault().language) {
                    "ru" -> course.title
                    else -> course.englishTitle.ifBlank { course.title }
                }
            }

            binding.summary.apply {
                text = course.summary
            }

            binding.price.apply {
                course.price
                    .takeUnless { it.isNullOrBlank() }
                    ?.let { text = it }
            }

            binding.buttonMore.setOnClickListener {
                listener.openCourseDetails(course)
            }
        }

        private fun setupCoverImageView(course: Course) {
            binding.cover.apply {
                Glide
                    .with(context.applicationContext)
                    .load(course.cover)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .centerCrop()
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable?>,
                            isFirstResource: Boolean
                        ): Boolean {
                            listener.onCourseImageLoadFailed(course)
                            this@apply.isInvisible = true
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable?>?,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            this@apply.isVisible = true
                            return false
                        }
                    })
                    .into(this)
            }
        }
    }


    companion object {
        private val DiffUtilCallback = object : DiffUtil.ItemCallback<Course>() {
            override fun areItemsTheSame(oldItem: Course, newItem: Course): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Course, newItem: Course): Boolean {
                return oldItem == newItem
            }
        }
    }
}


internal interface CourseItemListener {
    fun saveCourse(course: Course)

    fun openCourseDetails(course: Course)

    fun onCourseImageLoadFailed(course: Course)
}