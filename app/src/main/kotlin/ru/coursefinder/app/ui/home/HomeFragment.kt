package ru.coursefinder.app.ui.home

import android.content.res.Resources
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.coursefinder.app.R
import ru.coursefinder.app.databinding.FragmentHomeBinding
import ru.coursefinder.app.ui.adapters.CourseItemListener
import ru.coursefinder.app.ui.adapters.CoursesAdapter
import ru.coursefinder.app.utils.Action
import ru.coursefinder.app.utils.applyWindowInsets
import ru.coursefinder.app.utils.createSpacerDrawable
import ru.coursefinder.domain.model.Course
import ru.coursefinder.domain.model.OrderBy

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.applyWindowInsets(WindowInsetsCompat.Type.systemBars())

        viewModel.orderByLiveData.observe(viewLifecycleOwner) {
            binding.sortCoursesTitle.text = it.getDisplayableTitle(resources)
        }
        binding.sortCoursesButton.setOnClickListener {
            showOrderBySelectionDialog(
                initialSelectedItem = viewModel.orderByLiveData.value ?: HomeViewModel.defaultOrder,
                onConfirm = viewModel::updateOrderBy
            )
        }

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
                viewModel.saveCourse(course)
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


    private fun showOrderBySelectionDialog(
        initialSelectedItem: OrderBy,
        onConfirm: (OrderBy) -> Unit
    ) {
        val orderByVariants = listOf(
            OrderBy.Popularity(isAscending = true),
            OrderBy.Popularity(isAscending = false),
            OrderBy.Rating(isAscending = true),
            OrderBy.Rating(isAscending = false),
            OrderBy.PublishDate(isAscending = true),
            OrderBy.PublishDate(isAscending = false)
        )
        var selectedIndex = orderByVariants.indexOf(initialSelectedItem)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.sort_by_title)
            .setSingleChoiceItems(
                /* items = */ orderByVariants.map { it.getDisplayableTitle(resources) }.toTypedArray(),
                /* checkedItem = */ selectedIndex
            ) { dialog, which ->
                selectedIndex = which
            }
            .setPositiveButton(R.string.accept_button_title) { dialog, which ->
                onConfirm(orderByVariants[selectedIndex])
            }
            .setNeutralButton(R.string.cancel_button_title) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }


    private fun OrderBy.getDisplayableTitle(resources: Resources): String {
        val postfix = if (isAscending) R.string.ascending_postfix else R.string.descending_postfix
        val titleRes = when (this) {
            is OrderBy.Popularity -> R.string.sort_by_popularity
            is OrderBy.PublishDate -> R.string.sort_by_date
            is OrderBy.Rating -> R.string.sort_by_rating
        }
        return "${resources.getString(titleRes)} (${resources.getString(postfix)})"
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