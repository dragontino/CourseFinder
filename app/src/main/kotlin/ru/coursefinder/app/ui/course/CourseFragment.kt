package ru.coursefinder.app.ui.course

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.parseAsHtml
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.coursefinder.app.R
import ru.coursefinder.app.databinding.FragmentCourseBinding
import ru.coursefinder.app.utils.Action
import ru.coursefinder.app.utils.applyWindowInsets
import ru.coursefinder.app.utils.getDisplayableDateString
import ru.coursefinder.app.utils.parseToDate
import ru.coursefinder.domain.model.Course
import java.util.Locale

class CourseFragment : Fragment() {

    private var _binding: FragmentCourseBinding? = null
    private val binding get() = _binding!!

    val args: CourseFragmentArgs by navArgs()

    private val viewModel: CourseViewModel by viewModel {
        parametersOf(args.courseId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCourseBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.courseToolbar.applyWindowInsets(WindowInsetsCompat.Type.systemBars())

        viewModel.messageLiveData.observe(viewLifecycleOwner) { message ->
            Snackbar.make(view, message.text, Snackbar.LENGTH_LONG)
                .setTextColor(Color.BLACK)
                .setBackgroundTint(Color.WHITE)
                .setActionTextColor(Color.RED)
                .setTextMaxLines(2)
                .apply {
                    val action = message.action ?: Action(
                        title = context.getString(R.string.close),
                        actionBlock = ::dismiss
                    )
                    setAction(action.title) { action.actionBlock() }
                }
                .show()
        }

        viewModel.isLoadingLiveData.observe(viewLifecycleOwner) { isLoading ->
            binding.saveCourseButton.isEnabled = !isLoading
            binding.contentLayout.isVisible = !isLoading
            binding.contentLoadingProgressBar.isVisible = isLoading
        }

        binding.navigationButton.setOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.courseLiveData.observe(viewLifecycleOwner) {
            setupCourse(course = it)
        }
    }


    private fun setupCourse(course: Course) {
        binding.saveCourseButton.apply {
            when {
                course.isFavourite -> {
                    setImageResource(R.drawable.icon_bookmark_filled)
                    imageTintList = ColorStateList.valueOf(
                        requireContext().getColor(R.color.selected_item_color)
                    )
                }
                else -> {
                    setImageResource(R.drawable.icon_bookmark)
                    imageTintList = ColorStateList.valueOf(
                        requireContext().getColor(R.color.light_black)
                    )
                }
            }

            setOnClickListener {
                viewModel.saveCourse(course)
            }
        }

        binding.courseCover.apply {
            Glide
                .with(context.applicationContext)
                .load(course.cover)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(DrawableTransitionOptions.withCrossFade())
                .fitCenter()
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
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

        binding.title.text = when (Locale.getDefault().language) {
            "ru" -> course.title
            else -> course.englishTitle.ifBlank { course.title }
        }

        val author = course.authors.firstOrNull()
        binding.authorImage.isVisible = author != null
        binding.authorTitle.isVisible = author != null
        binding.authorName.isVisible = author != null

        binding.authorImage.takeIf { author != null }?.apply {
            Glide
                .with(context.applicationContext)
                .load(author!!.avatarUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(DrawableTransitionOptions.withCrossFade())
                .circleCrop()
                .into(this)
        }

        author?.fullName?.let { binding.authorName.text = it }

        binding.buttonStartCourse.setOnClickListener {
            openLink(course.startCourseUrl)
        }

        binding.goToPlatform.setOnClickListener {
            openLink(course.canonicalUrl)
        }

        binding.courseDescription.text = course.description.parseAsHtml()
    }


    private fun openLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}