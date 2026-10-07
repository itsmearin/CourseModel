import com.example.intellipaat.repository.CourseRepository
import com.example.intellipaat.ui.DashboardViewModel

class DashboardViewModelFactory(private val repository: CourseRepository) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return DashboardViewModel(repository) as T
    }
}