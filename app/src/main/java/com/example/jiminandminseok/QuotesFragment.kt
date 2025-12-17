package com.example.jiminandminseok

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.jiminandminseok.databinding.FragmentQuotesBinding

data class Quote(val text: String, val author: String)

class QuotesFragment : Fragment() {

    private var _binding: FragmentQuotesBinding? = null
    private val binding get() = _binding!!

    private val quotes = listOf(
        Quote("가장 큰 영광은 한 번도 실패하지 않음이 아니라, 실패할 때마다 다시 일어서는 데에 있다.", "- 공자 -"),
        Quote("성공의 비결은 단 한 가지, 포기하지 않는 것이다.", "- 윈스턴 처칠 -"),
        Quote("우리의 가장 큰 약점은 포기하는 것에 있다. 성공하는 가장 확실한 방법은 항상 한 번 더 시도해 보는 것이다.", "- 토머스 에디슨 -"),
        Quote("습관은 인간의 삶에 있어 가장 큰 영향을 미치는 것 중 하나이다.", "- 아리스토텔레스 -"),
        Quote("오늘의 작은 실천이 내일의 큰 변화를 만든다.", "- 미상 -"),
        Quote("담배를 이기는 것은 나 자신을 이기는 것이다.", "- 금연 커뮤니티 -"),
        Quote("금연은 길고 힘든 싸움이지만, 그 끝에는 건강과 자유가 있다.", "- 미상 -")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setRandomQuote()

        binding.btnNewQuote.setOnClickListener {
            setRandomQuote()
        }
    }

    private fun setRandomQuote() {
        val randomQuote = quotes.random()
        binding.tvQuoteText.text = randomQuote.text
        binding.tvQuoteAuthor.text = randomQuote.author
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
