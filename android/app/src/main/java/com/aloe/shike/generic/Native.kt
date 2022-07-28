package com.aloe.shike.generic

object Native {
  init {
      System.loadLibrary("native")
  }
  external fun hello():String
}
