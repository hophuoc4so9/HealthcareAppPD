import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthcareapppd.R
import com.example.healthcareapppd.databinding.FragmentHomeBinding
import com.example.healthcareapppd.domain.usecase.Article

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.layoutDoctor.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_docter)
        }
        binding.layoutMap.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_map)
        }

        setupArticleRecyclerView()
    }

    private fun setupArticleRecyclerView() {
        val articles = listOf(
            Article("Lợi ích của việc đi bộ mỗi ngày", "Đi bộ 30 phút mỗi ngày giúp cải thiện sức khỏe tim mạch, giảm căng thẳng và kiểm soát cân nặng hiệu quả."),
            Article("Uống đủ nước quan trọng thế nào?", "Nước chiếm 70% cơ thể, việc uống đủ 2 lít nước mỗi ngày giúp thanh lọc cơ thể và làm đẹp da."),
            Article("Giấc ngủ và sức khỏe tinh thần", "Ngủ đủ 7-8 tiếng mỗi đêm là yếu tố quan trọng để phục hồi năng lượng và duy trì sự minh mẫn."),
            Article("5 loại thực phẩm tốt cho não bộ", "Cá hồi, quả việt quất, nghệ, bông cải xanh và hạt bí ngô giúp tăng cường trí nhớ và sự tập trung."),
            Article("Cách giảm stress trong công việc", "Thiền, hít thở sâu và dành thời gian cho sở thích cá nhân là những phương pháp hiệu quả để giảm căng thẳng.")
        )

        val articleAdapter = ArticleAdapter(articles)

        binding.recyclerArticles.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = articleAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}