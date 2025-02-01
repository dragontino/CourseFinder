package ru.coursefinder.app.ui.favourites

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.coursefinder.app.R
import ru.coursefinder.app.databinding.FragmentFavouritesBinding
import ru.coursefinder.app.ui.adapters.CourseItemListener
import ru.coursefinder.app.ui.adapters.CoursesAdapter
import ru.coursefinder.app.ui.home.HomeFragmentDirections
import ru.coursefinder.app.utils.Action
import ru.coursefinder.app.utils.applyWindowInsets
import ru.coursefinder.app.utils.createSpacerDrawable
import ru.coursefinder.domain.model.Course

class FavouriteCoursesFragment : Fragment() {
    private var _binding: FragmentFavouritesBinding? = null
    private  val binding get() = _binding!!

    private val viewModel: FavouriteCoursesViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFavouritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.applyWindowInsets(WindowInsetsCompat.Type.systemBars())

        setupCoursesRecyclerView()

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
    }


    private val coursesLoadStateListener by lazy {
        fun (loadStates: CombinedLoadStates) {
            val adapter = binding.coursesRecyclerView.adapter as PagingDataAdapter<*, *>

            binding.coursesRecyclerView.isVisible =
                loadStates.source.refresh is LoadState.NotLoading && adapter.itemCount > 0

            binding.emptyListPlaceholder.isVisible = binding.coursesRecyclerView.isVisible.not()
                    && loadStates.source.refresh !is LoadState.Loading

            binding.contentLoadingProgressBar.isVisible =
                loadStates.source.refresh is LoadState.Loading ||
                        loadStates.source.append is LoadState.Loading

            arrayOf(loadStates.source.refresh, loadStates.source.append).forEach { loadState ->
                if (loadState is LoadState.Error) {
                    loadState.error.localizedMessage?.let {
                        val retryAction = Action(requireContext().getString(R.string.retry)) {
                            adapter.retry()
                        }
                        viewModel.sendMessage(it, retryAction)
                    }
                }
            }
        }
    }

    private fun setupCoursesRecyclerView() {
        val coursesAdapter = CoursesAdapter(object : CourseItemListener {
            override fun saveCourse(course: Course) {
                viewModel.removeSavedCourse(course)
            }

            override fun openCourseDetails(course: Course) {
                val action = HomeFragmentDirections.showCourseDetailsAction(courseId = course.id)
                findNavController().navigate(action)
            }

            override fun onCourseImageLoadFailed(course: Course) {
                viewModel.sendMessage(
                    message = getString(R.string.course_cover_image_load_failed, course.title)
                )
            }
        })
        coursesAdapter.addLoadStateListener(coursesLoadStateListener)

        binding.coursesRecyclerView.adapter = coursesAdapter
        binding.coursesRecyclerView.addItemDecoration(
            DividerItemDecoration(requireContext(), RecyclerView.VERTICAL).apply {
                setDrawable(createSpacerDrawable(spaceDp = 16))
            }
        )

        viewModel.coursesFlow
            .onEach(coursesAdapter::submitData)
            .launchIn(viewModel.viewModelScope)
    }


    override fun onStop() {
        (binding.coursesRecyclerView.adapter as PagingDataAdapter<*, *>)
            .removeLoadStateListener(coursesLoadStateListener)
        super.onStop()
    }


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}