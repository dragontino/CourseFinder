package ru.coursefinder.app.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
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
import ru.coursefinder.app.databinding.FragmentHomeBinding
import ru.coursefinder.app.ui.adapters.CourseItemListener
import ru.coursefinder.app.ui.adapters.CoursesAdapter
import ru.coursefinder.app.utils.Action
import ru.coursefinder.app.utils.applyWindowInsets
import ru.coursefinder.app.utils.createSpacerDrawable
import ru.coursefinder.domain.model.Course

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
        binding.coursesRecyclerView.applyWindowInsets(WindowInsetsCompat.Type.systemBars())
        binding.emptyListPlaceholder.applyWindowInsets(WindowInsetsCompat.Type.systemBars())

        setupCoursesRecyclerView()

        viewModel.messageLiveData.observe(viewLifecycleOwner) { message ->
            val duration = when (message.action) {
                null -> Snackbar.LENGTH_INDEFINITE
                else -> Snackbar.LENGTH_LONG
            }
            Snackbar.make(view, message.text, duration)
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
                    throw loadState.error
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
            override fun addCourseToFavourite(course: Course) {
                viewModel.sendMessage("Курс теперь в избранном")
            }

            override fun removeCourseFromFavourites(course: Course) {
                viewModel.sendMessage("Курс больше не избранный")
            }

            override fun openCourseDetails(course: Course) {
                viewModel.sendMessage("Открываем курс")
            }

            override fun onCourseImageLoadFailed(course: Course) {
                viewModel.sendMessage(message = "Не удалось загрузить изображение для курса ${course.title}")
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