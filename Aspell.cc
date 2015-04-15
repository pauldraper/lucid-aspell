// Based off the work by: https://github.com/samiljan/aspell-android

#include "Aspell.h"
#include <aspell.h>
#include <algorithm>
#include <sstream>

const int maxSuggestions = 10;

JNIEXPORT jboolean JNICALL Java_com_lucidchart_aspell_Aspell_init(JNIEnv* env, jobject thisObj, jstring language) {
  AspellSpeller*      speller   = NULL;
  AspellConfig*       config    = NULL;
  AspellCanHaveError* spellErr  = NULL;
  jclass              jc        = env->GetObjectClass(thisObj);
  jfieldID            fieldID   = env->GetFieldID(jc, "aspellPtr", "J");
  const char*         lang      = env->GetStringUTFChars(language, NULL);

  if (env->GetLongField(thisObj, fieldID) != 0) return JNI_FALSE;

  // Configure aspell
  // List of configurations: http://aspell.net/0.50-doc/man-html/4_Customizing.html
  config = new_aspell_config();
  aspell_config_replace(config, "lang", lang);
  aspell_config_replace(config, "encoding", "UTF-8");
  
  spellErr = new_aspell_speller(config);

  delete_aspell_config(config);
  env->ReleaseStringUTFChars(language, lang);

  if (aspell_error(spellErr) != 0) {
    delete_aspell_can_have_error(spellErr);
    return JNI_FALSE;
  }

  speller = to_aspell_speller(spellErr);
  config = aspell_speller_config(speller);
  env->SetLongField(thisObj, fieldID, (jlong) speller);
  
  return JNI_TRUE;
}

JNIEXPORT void JNICALL Java_com_lucidchart_aspell_Aspell_cleanup(JNIEnv* env, jobject thisObj) {
  jclass          jc            = env->GetObjectClass(thisObj);
  jfieldID        fieldID       = env->GetFieldID(jc, "aspellPtr", "J");
  AspellSpeller*  speller       = (AspellSpeller*)env->GetLongField(thisObj, fieldID);

  delete_aspell_speller(speller);
  env->SetLongField(thisObj, fieldID, 0);
}

JNIEXPORT jobjectArray JNICALL Java_com_lucidchart_aspell_Aspell_check(JNIEnv* env, jobject thisObj, jstring word) {
  jobjectArray              res;
  jclass                    jc              = env->GetObjectClass(thisObj);
  jfieldID                  fieldID         = env->GetFieldID(jc, "aspellPtr", "J");
  AspellSpeller*            speller         = (AspellSpeller *)env->GetLongField(thisObj, fieldID);
  int                       found           = 100;
  const char*               cWord           = NULL;
  const AspellWordList*     wl              = NULL;
  int                       numSuggestions  = 0;
  AspellStringEnumeration*  els; 
  const char*               suggestion;
  int                       suggestionCtr   = 0;

  if (speller == NULL) {
    res = (jobjectArray) env->NewObjectArray(1, env->FindClass("java/lang/String"), env->NewStringUTF(""));
    env->SetObjectArrayElement(res, 0, env->NewStringUTF("-100"));
    return res;
  }

  cWord = env->GetStringUTFChars(word, NULL);
  found = aspell_speller_check(speller, cWord, -1);
  std::ostringstream errStr; 
  errStr << found;

  if (found != 0 && found != 1) { 
    res = (jobjectArray) env->NewObjectArray(1, env->FindClass("java/lang/String"), env->NewStringUTF(""));
    env->SetObjectArrayElement(res, 0, env->NewStringUTF(errStr.str().c_str()));
  } else { 
    wl = aspell_speller_suggest(speller, cWord, -1);
    numSuggestions = aspell_word_list_size(wl) + 1;
    res = (jobjectArray) env->NewObjectArray(std::min(maxSuggestions, numSuggestions), env->FindClass("java/lang/String"), env->NewStringUTF(""));
    els = aspell_word_list_elements(wl);
    env->SetObjectArrayElement(res, 0, env->NewStringUTF(errStr.str().c_str()));

    for (suggestionCtr= 1; suggestionCtr < std::min(maxSuggestions, numSuggestions); suggestionCtr++) {
      suggestion = aspell_string_enumeration_next(els);
      env->SetObjectArrayElement(res, suggestionCtr, env->NewStringUTF(suggestion));
    }
    delete_aspell_string_enumeration(els);
  }
  env->ReleaseStringUTFChars(word, cWord);
  return res;
}

JNIEXPORT void JNICALL Java_com_lucidchart_aspell_Aspell_addUserWords(JNIEnv* env, jobject thisObj, jobjectArray words) {
  jclass          jc        = env->GetObjectClass(thisObj);
  jfieldID        fieldID   = env->GetFieldID(jc, "aspellPtr", "J");
  AspellSpeller*  speller   = (AspellSpeller*)env->GetLongField(thisObj, fieldID);
  jstring         jword;
  int             wordCtr   = 0;
  const char*     userWord;
  int             wordLength;
  
  if (speller == NULL) return;

  aspell_speller_clear_session(speller);
  
  for (wordCtr = 0; wordCtr < env->GetArrayLength(words); wordCtr++) {
    jword       = (jstring)env->GetObjectArrayElement(words, wordCtr);
    userWord    = env->GetStringUTFChars(jword, NULL);
    wordLength  = env->GetStringUTFLength(jword);

    aspell_speller_add_to_session(speller, userWord, wordLength);
    env->ReleaseStringUTFChars(jword, userWord);
  }
}
