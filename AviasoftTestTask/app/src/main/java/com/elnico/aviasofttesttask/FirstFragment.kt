package com.elnico.aviasofttesttask

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.elnico.aviasofttesttask.databinding.FragmentFirstBinding
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Base64
import java.util.Date
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        val token = apiKey()
        println(token)

        //////////////////////

        Thread {
            val postData = "{\n" +
                    "\"jsonrpc\" : \"2.0\",\n" +
                    "\"method\" : \"trips.show\",\n" +
                    "\"params\" : {\n" +
                    "\"id\": \"10\",\n" +
                    "\"company\": \"wa\"\n" +
                    "},\n" +
                    "\"id\":1\n" +
                    "}"

            val myURL = URL("https://testpos.api.skydepot.io/")
            val myURLConnection = myURL.openConnection() as HttpURLConnection

            myURLConnection.requestMethod = "POST"

            //val timestamp = (Date().time / 1000).toString()
            val timestamp = (Date().time).toString()

            myURLConnection.setRequestProperty("rts", timestamp)
            myURLConnection.setRequestProperty("authorization", timestamp)
            myURLConnection.setRequestProperty("Content-Type", "application/json")

            //myURLConnection.setRequestProperty("Content-Length", "" + java.lang.String(postData).bytes.size)
            myURLConnection.useCaches = false
            myURLConnection.doInput = true
            myURLConnection.doOutput = true

            myURLConnection.outputStream.use { os ->
                val input: ByteArray = java.lang.String(postData).getBytes("utf-8")
                os.write(input, 0, input.size)
            }

            BufferedReader(
                InputStreamReader(myURLConnection.inputStream, "utf-8")
            ).use { br ->
                var response = StringBuilder()

                var responseLine: String? = null
                while ((br.readLine().also { responseLine = it }) != null) {
                    response.append(responseLine!!.trim { it <= ' ' })
                }

                System.out.println(response.toString())
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun apiKey(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val ctime = formatter.format(Date())
        println(ctime)

        val sourceA = "${(ctime[13].code + 1) *
                (ctime[15].code + 1)}TEST${ctime.substring(0, 17)}TASK${ctime.substring(0, 17)}"

        val sourceB = "DOIT${ctime.substring(12, 17)}PLS${ctime.substring(12, 17)}${(ctime[13].code + 1) * (ctime[15].code + 3)}"

        ////////////////

        val md = MessageDigest.getInstance("SHA-512")

        val digestA = md.digest(sourceA.toByteArray())
        val sbA = StringBuilder()
        for (i in digestA.indices) {
            sbA.append(((digestA[i].toInt() and 0xff) + 0x100).toString(16).substring(1))
        }

        val digestB = md.digest(sourceB.toByteArray())
        val sbB = StringBuilder()
        for (i in digestB.indices) {
            sbB.append(((digestB[i].toInt() and 0xff) + 0x100).toString(16).substring(1))
        }

        ////////////////

        val key = sbA.toString().substring(0, 32).uppercase()
        val iv = sbB.toString().substring(16, 32).uppercase()

        ////////////////

        val c = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val sk = SecretKeySpec(java.lang.String(key).bytes, "AES")
        val aesIV = IvParameterSpec(java.lang.String(iv).bytes)
        c.init(Cipher.ENCRYPT_MODE, sk, aesIV)

        val apiKey = "${encrypt(c, key).trim()}${encrypt(c, "justfortesttask").trim()}${encrypt(c, iv).trim()}"

        println(sourceA)
        println(sourceB)
        println(key)
        println(iv)

        println(apiKey)

        return apiKey
    }
}

private fun encrypt(cipher: Cipher, value: String): String {
    val encrypted: ByteArray = cipher.doFinal(java.lang.String(value).bytes)
    return Base64.getEncoder().encodeToString(encrypted)
}