package no.nav.sbl.sosialhjelpmodiaapi.utils

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.tika.Tika
import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest

fun getSha512FromByteArray(bytes: ByteArray?): String {
    if (bytes == null) {
        return ""
    }

    val md = MessageDigest.getInstance("SHA-512")
    val digest = md.digest(bytes)
    return digest.fold("", { str, it -> str + "%02x".format(it) })
}

fun isPdf(inputStream: InputStream): Boolean {
    return Tika().detect(inputStream).equals("application/pdf", ignoreCase = true)
}

fun isImage(inputStream: InputStream): Boolean {
    val type = Tika().detect(inputStream)
    return type == "image/png" || type == "image/jpeg"
}

fun pdfIsSigned(pdf: PDDocument): Boolean {
    try {
        return pdf.signatureDictionaries.isNotEmpty()
    } catch (var3: IOException) {
        throw RuntimeException("Kunne ikke lese signaturinformasjon fra PDF")
    }

}