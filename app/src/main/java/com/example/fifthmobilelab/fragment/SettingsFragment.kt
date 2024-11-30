package com.example.fifthmobilelab.fragment

import android.os.Bundle
import android.os.Environment
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.fifthmobilelab.AppPreferences
import com.example.fifthmobilelab.FontSizeApplier
import com.example.fifthmobilelab.databinding.FragmentSettingsBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load and apply settings
        lifecycleScope.launch {
            try {
                // Theme
                val isDarkMode = AppPreferences.isDarkMode(requireContext()).first()
                applyTheme(isDarkMode)
                binding.themeRadioGroup.check(if (isDarkMode) binding.radioDark.id else binding.radioLight.id)

                // Notifications
                binding.notificationsSwitch.isChecked =
                    AppPreferences.areNotificationsEnabled(requireContext()).first()

                // Font Size
                val fontSize = AppPreferences.getFontSize(requireContext()).first()
                binding.fontSizeRadioGroup.check(
                    when (fontSize) {
                        "Small" -> binding.radioSmall.id
                        "Medium" -> binding.radioMedium.id
                        "Large" -> binding.radioLarge.id
                        else -> binding.radioMedium.id
                    }
                )

                // Language
                val languages = listOf("English", "Spanish", "French", "German")
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    languages
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.languageSpinner.adapter = adapter
                val language = AppPreferences.getLanguage(requireContext()).first()
                binding.languageSpinner.setSelection(languages.indexOf(language))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Theme Selection
        binding.themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.radioLight.id -> {
                    saveThemePreference(false)
                    applyTheme(false)
                }
                binding.radioDark.id -> {
                    saveThemePreference(true)
                    applyTheme(true)
                }
            }
        }

        // Notifications
        binding.notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                AppPreferences.setNotifications(requireContext(), isChecked)
            }
        }

        // Font Size
        binding.fontSizeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedFontSize = when (checkedId) {
                binding.radioSmall.id -> "Small"
                binding.radioMedium.id -> "Medium"
                binding.radioLarge.id -> "Large"
                else -> "Medium"
            }
            lifecycleScope.launch {
                AppPreferences.setFontSize(requireContext(), selectedFontSize)
                (activity as? FontSizeApplier)?.applyFontSize(selectedFontSize)
            }
        }

        // Language
        binding.languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedLanguage = parent?.getItemAtPosition(position).toString()
                lifecycleScope.launch {
                    AppPreferences.setLanguage(requireContext(), selectedLanguage)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // File Management
        checkFileStatus()

        binding.btnDeleteFile.setOnClickListener {
            backupFileBeforeDelete()
            deleteFile()
        }

        binding.btnRestoreFile.setOnClickListener {
            restoreFile()
        }
    }

    private fun applyTheme(isDarkMode: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun saveThemePreference(isDarkMode: Boolean) {
        lifecycleScope.launch {
            AppPreferences.setDarkMode(requireContext(), isDarkMode)
        }
    }

    private fun checkFileStatus() {
        val fileDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(fileDir, "5.txt")

        if (file.exists()) {
            binding.fileStatus.text = "Статус файла: Найден"
            binding.btnDeleteFile.visibility = View.VISIBLE
        } else {
            binding.fileStatus.text = "Статус файла: Не найден"
            binding.btnDeleteFile.visibility = View.GONE
        }

        checkBackupStatus()
    }

    private fun checkBackupStatus() {
        val internalStorage = requireContext().filesDir
        val backupFile = File(internalStorage, "backup_5.txt")

        binding.btnRestoreFile.visibility = if (backupFile.exists()) View.VISIBLE else View.GONE
    }


    private fun deleteFile() {
        val fileDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(fileDir, "5.txt")

        if (file.exists()) {
            file.delete()
            Toast.makeText(context, "Файл удалён", Toast.LENGTH_SHORT).show()
            checkFileStatus()
        } else {
            Toast.makeText(context, "Файл не найден", Toast.LENGTH_SHORT).show()
        }
    }

    private fun backupFileBeforeDelete() {
        val fileDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val sourceFile = File(fileDir, "5.txt")

        if (sourceFile.exists()) {
            val internalStorage = requireContext().filesDir
            val backupFile = File(internalStorage, "backup_5.txt")
            sourceFile.copyTo(backupFile, overwrite = true)
            Toast.makeText(context, "Резервная копия создана", Toast.LENGTH_SHORT).show()
            checkBackupStatus()
        }
    }


    private fun restoreFile() {
        val internalStorage = requireContext().filesDir
        val backupFile = File(internalStorage, "backup_5.txt")

        if (backupFile.exists()) {
            val fileDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val restoredFile = File(fileDir, "5.txt")
            backupFile.copyTo(restoredFile, overwrite = true)
            Toast.makeText(context, "Файл восстановлен", Toast.LENGTH_SHORT).show()
            backupFile.delete()
            checkBackupStatus()
            checkFileStatus()
        } else {
            Toast.makeText(context, "Резервная копия отсутствует", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
