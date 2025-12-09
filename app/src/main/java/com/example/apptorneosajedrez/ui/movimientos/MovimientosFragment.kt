package com.example.apptorneosajedrez.ui.movimientos

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apptorneosajedrez.databinding.FragmentMovimientosBinding
import com.example.apptorneosajedrez.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class MovimientosFragment : Fragment() {

    private var _binding: FragmentMovimientosBinding? = null
    private val binding get() = _binding!!

    private val vm: MovimientosViewModel by viewModels()
    private lateinit var adapter: MovimientosAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovimientosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) Comprobación de sesión
        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
            return
        }

        // 2) RecyclerView + adapter
        adapter = MovimientosAdapter(emptyList())
        binding.rvMoves.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MovimientosFragment.adapter
        }

        // 3) Observación del ViewModel (usar viewLifecycleOwner en Fragments)
        vm.moves.observe(viewLifecycleOwner) { list ->
            adapter.update(list)
            if (list.isNotEmpty()) {
                binding.rvMoves.scrollToPosition(list.size - 1)
            }
        }

        // 4) Regex y filtros para los movimientos
        val chessMoveRegex = Regex(
            """^(?:(?:O-O(?:-O)?)|(?:[RDTAC]?[a-h]?[1-8]?x?[a-h][1-8](?:=[RDTAC])?))[+#]?(?:[!?]{1,2})?$"""
        )

        val chessCharFilter = InputFilter { source, _, _, _, _, _ ->
            val allowed = "abcdefghABCDEFGH12345678xX=NBRQnbrq+#Oo-"
            if (source.all { it in allowed }) source else ""
        }
        binding.etMoveInput.filters = arrayOf(chessCharFilter)

        binding.btnSend.isEnabled = false

        binding.etMoveInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int
            ) { }

            override fun onTextChanged(
                s: CharSequence?, start: Int, before: Int, count: Int
            ) {
                val input = s.toString().trim()
                binding.btnSend.isEnabled = chessMoveRegex.matches(input)
            }

            override fun afterTextChanged(s: android.text.Editable?) { }
        })

        binding.btnSend.setOnClickListener {
            val text = binding.etMoveInput.text.toString().trim()
            vm.sendMove(text)
            binding.etMoveInput.text?.clear()
        }

        // 5) Ajuste de insets para el layout de entrada (equivalente a Activity)
        ViewCompat.setOnApplyWindowInsetsListener(binding.llInput) { v, insets ->
            val navBarHeight = insets
                .getInsets(WindowInsetsCompat.Type.navigationBars())
                .bottom
            v.updatePadding(bottom = navBarHeight)
            insets
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
