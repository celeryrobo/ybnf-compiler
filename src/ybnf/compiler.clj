(ns ybnf.compiler
  (:gen-class
    :name ybnf.compiler
    :init "init"
    :state "state"
    :prefix "grammar-"
    :constructors {[String] []}
    :methods [[isFailure [] boolean]
              [getFailure [] String]
              [getGrammar [] String]
              [mergeGrammar [String] void]
              [getHeader [] java.util.Map]
              [getCallable [] java.util.ArrayList]
              [runCallable [String] String]
              [getKeyValue [] clojure.lang.APersistentMap]
              [mergeKeyValue [clojure.lang.APersistentMap] void]
              [execCompile [String] java.util.Map]])
  (:import java.util.HashMap
           java.util.ArrayList)
  (:require [instaparse.core :as insta]
            [ybnf.grammar :as gm]
            [clojure.string :as cs]))

(defn grammar-init
  [^String grammar]
  (let [tree (gm/grammar-compile grammar)
        [header callable lang] (if (insta/failure? tree) [nil nil nil] (gm/grammar-parser tree))
        keyvalue (if (insta/failure? tree) {} (gm/grammar-keyvalue tree))]
    [[] (doto (java.util.HashMap.)
              (.put "tree" tree)
              (.put "header" header)
              (.put "lang" lang)
              (.put "keyvalue" keyvalue)
              (.put "callable" callable))]))

(defn grammar-isFailure
  [this]
  (insta/failure? (.get (.state this) "tree")))

(defn grammar-getFailure
  [this]
  (let [fail (insta/get-failure (.get (.state this) "tree"))]
    (str "index : " (:index fail)
         "\nline : " (:line fail)
         "\ncolumn : " (:column fail)
         "\ntext : " (:text fail)
         "\nreason : " (cs/join "; " (map (fn [args] (:expecting args)) (:reason fail))))))

(defn grammar-getGrammar
  [this]
  (.get (.state this) "lang"))

(defn grammar-mergeGrammar
  [this ^String grammar]
  (.put (.state this) "lang" (str (.getGrammar this) "\n" (or grammar ""))))

(defn grammar-getKeyValue
  [this]
  (.get (.state this) "keyvalue"))

(defn grammar-mergeKeyValue
  [this kv]
  (.put (.state this) "keyvalue" (into (.getKeyValue this) kv)))

(defn grammar-getCallable
  [this]
  (.get (.state this) "callable"))

(defn grammar-getHeader
  [this]
  (let [header (.get (.state this) "header")
        heads (java.util.HashMap.)]
    (if (nil? header) heads
      (loop [[[k v] & hs] header]
        (if (.containsKey heads (name k))
          (.add (.get heads (name k)) v)
          (.put heads (name k) (doto (java.util.ArrayList.) (.add v))))
        (if (nil? hs) heads (recur hs))))))

(defn grammar-runCallable
  [this ^String text]
  "")

(defn grammar-execCompile
  [this ^String text]
  (let [grammar-kv (.getKeyValue this)
        grammar-tree (gm/grammar-compile (str (.getGrammar this) "\n" (.runCallable this text)) text)]
    (if (insta/failure? grammar-tree)
      (let [fail (insta/get-failure grammar-tree)]
        (throw (Exception.
          (str "index : " (:index fail)
               "\nline : " (:line fail)
               "\ncolumn : " (:column fail)
               "\ntext : " (:text fail)
               "\nreason : " (cs/join "; " (map (fn [args] (:expecting args)) (:reason fail)))))))
      (let [[objects slots] (gm/ybnf-parser (gm/ybnf-keyword grammar-tree) grammar-kv)]
        (doto (java.util.HashMap.) (.put "objects" objects) (.put "slots" slots))))))

