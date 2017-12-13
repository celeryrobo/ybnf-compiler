(ns ybnf.jutils
  (:gen-class
   :methods [#^{:static true} [getGrammar [] String]])
  (:require [ybnf.grammar :as gm]))

(defn -getGrammar
  []
  gm/grammar-lang)
