package com.lucidchart.aspell

import scala.util.Try

private[aspell] class Aspell {

  // initialize aspell for a given language
  @native def init(language: String): Boolean

  // clean up allocations on heap/other leakable structures
  @native def cleanup()

  // Check a word and return suggestions, if any
  @native def check(word: String): Array[String]

  // Add user words to dictionary
  @native def addUserWords(words: Array[String])

  // This is going to be cast to an aspell pointer in the native(C and C++) code.
  var aspellPtr: Long = 0;
}

/**
 * Case class representation of the result sent back from the aspell library.
 *
 * @param word the word that was checked
 * @param valid whether or not the word was correctly spelled
 * @param suggestions if the word is not valid, this array will contain suggested spellings
 */
case class WordSuggestions(word: String, valid: Boolean, suggestions: Array[String])

/**
 * The Aspell object provides the public interface for the lucid-aspell library. It loads the native
 * library and has a method to check the spelling on a word.
 */
object Aspell {
  NativeLibraryLoader.load(s"/${BuildInfo.libraryName}.so")

  /**
   * Check the spelling for each word in an array of words
   *
   * @param language the language code for the dictionary to use (ie, en, es, fr, etc)
   * @param words the words to check
   * @param userWords the custom words that the user has created to augment the dictionary of
   * correct words
   * @return an array of WordSuggestions object, with one WordSuggestions for each word that was
   * checked
   */
  def check(language: String, words: Array[String], userWords: Array[String]): Array[WordSuggestions] = {
    val aspell = new Aspell
    aspell.init(language)

    if (!userWords.isEmpty) {
      aspell.addUserWords(userWords)
    }
    val checks = words.map { word =>
      val wordCheck = aspell.check(word)
      val valid = Try {
        if (wordCheck(0).toInt == 1) true else false
      }.getOrElse(false)
      if (wordCheck.length >= 1) {
        WordSuggestions(word, valid, wordCheck.slice(1, wordCheck.length))
      }
      else {
        WordSuggestions(word, false, Array.empty)
      }
    }

    aspell.cleanup
    checks
  }
}
