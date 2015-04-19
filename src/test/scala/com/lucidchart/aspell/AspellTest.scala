package com.lucidchart.aspell

import org.specs2.mutable._

class AspellSpec extends Specification {

  "Aspell" should {

    "work" in {
      Aspell.check("", Array(), Array())
      ok
    }

  }

}
