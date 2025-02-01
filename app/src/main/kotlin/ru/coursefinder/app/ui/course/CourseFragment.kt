package ru.coursefinder.app.ui.course

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        viewModel.courseLiveData.observe(viewLifecycleOwner) { course ->
            binding.saveCourseButton.apply {
                when {
                    course.isFavourite -> setImageResource(R.drawable.icon_bookmark_filled)
                    else -> {
                        setImageResource(R.drawable.icon_bookmark)
                        imageTintList = ColorStateList.valueOf(
                            requireContext().getColor(R.color.light_black)
                        )
                    }
                }

                setOnClickListener {
                    when {
                        course.isFavourite -> viewModel.removeCourseFromSaved()
                        else -> viewModel.saveCourse()
                    }
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
        }
    }


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}