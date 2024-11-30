import android.content.ContentValues
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fifthmobilelab.R
import com.example.fifthmobilelab.databinding.FragmentCharacterListBinding
import com.example.fifthmobilelab.fragment.SettingsFragment
import io.ktor.client.HttpClient
import kotlinx.coroutines.*
import kotlinx.serialization.json.JsonNull.content
import java.io.File

class CharacterFragment : Fragment() {

    private var _binding: FragmentCharacterListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CharacterAdapter
    private val repository = CharacterRepository(HttpClient())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCharacterListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация layoutManager и пустого адаптера для RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = CharacterAdapter(emptyList()) // Пустой список для инициализации
        binding.recyclerView.adapter = adapter

        // Переход в настройки при нажатии на кнопку
        binding.btnSettings.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SettingsFragment())
                .addToBackStack(null) // Добавляем в стек, чтобы можно было вернуться назад
                .commit()
        }

        // Затем начинаем загрузку данных
        fetchCharacters()
    }


    private fun fetchCharacters() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val characters = repository.getCharacters(page = 5)
                withContext(Dispatchers.Main) {
                    adapter = CharacterAdapter(characters) // Обновляем адаптер новыми данными
                    binding.recyclerView.adapter = adapter // Устанавливаем адаптер с данными

                    saveCharactersToFile(characters, "5.txt")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    if (isAdded) { // Проверяем, что фрагмент всё ещё активен
                        Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun saveCharactersToFile(characters: List<Character>, fileName: String) {
        try {
            val contentResolver = requireContext().contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS) // Сохраняем в /Downloads
            }

            val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                contentResolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(characters.joinToString("\n") { it.toString() }.toByteArray())
                    Toast.makeText(context, "Файл сохранён в /Downloads", Toast.LENGTH_SHORT).show()
                } ?: Toast.makeText(context, "Ошибка открытия потока для записи", Toast.LENGTH_SHORT).show()
            } ?: Toast.makeText(context, "Ошибка сохранения файла", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
