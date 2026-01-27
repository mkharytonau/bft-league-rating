package com.mkharytonau.rating

import java.util.jar.Attributes.Name
import com.ibm.icu.text.Transliterator
import org.apache.commons.text.similarity.JaroWinklerSimilarity

object NameMatcher {
  val transliterator = Transliterator.getInstance("Cyrillic-Latin; Latin-ASCII")

  def translit(s: String): String = transliterator.transliterate(s)

  val jw = new JaroWinklerSimilarity()

  def normalize(raw: String): String =
    raw.toLowerCase
      .replace("-", " ")
      .replaceAll("[^a-z\\s]", "")
      .trim
      .split("\\s+")
      .sorted
      .mkString(" ")

  def prepare(name: String): String =
    normalize(translit(name))

  def apply(name1: String, name2: String): Boolean = {
    val similarity = jw.apply(prepare(name1), prepare(name2))
		// println(s"Comparing $name1(${prepare(name1)}) and $name2(${prepare(name2)}), result: $similarity")
		similarity > 0.8
  }
}
