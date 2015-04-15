package com.lucidchart.aspell

class Aspell { 

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

case class WordSuggestions(word: String, valid: Boolean, suggestions: Array[String])

object Aspell { 
  System.loadLibrary("lucidaspell")
  def check(language: String, words: Array[String], userWords: Array[String]): Array[WordSuggestions] = {
    val aspell = new Aspell
    aspell.init(language)
    if (!userWords.isEmpty) 
      aspell.addUserWords(userWords)
    val checks = words.map{ word =>
      val wordCheck = aspell.check(word)
      val valid = if (wordCheck(0).toInt == 1) true else false
      if (wordCheck.length >= 1)
        WordSuggestions(word, valid, wordCheck.slice(1, wordCheck.length))
      else
        WordSuggestions(word, false, Array())
    }
    aspell.cleanup
    checks
  }
}
