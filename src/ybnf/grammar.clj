(ns ybnf.grammar
  (:import java.util.HashMap)
  (:require [instaparse.core :as insta]
            [clojure.string :as cs]))

(def grammar-lang
  "root = head [callable] [mainArg] body
   body = {define}

   head = <'#YBNF'> space version space charset semicolon {include} [modeArg]
   version = #'\\d+\\.\\d+'
   charset = 'utf8' | 'UTF8' | 'utf-8' | 'UTF-8'
   include = <'#include'> space filename semicolon
   filename = #'.+\\.ybnf'
   <modeArg> = <'service'> space service semicolon
   service = word

   callable = <'callable'> space call {comma call} semicolon
   call = <'@'> word <'('> {space} {word} {comma word} {space} <')'>

   <mainArg> = <'root'> space main semicolon
   main = variable

   define = {space} varname {space} <'='> {space} sentence semicolon
   varname = variable [<'{'> key [<'%'> value] <'}'>]
   variable = <'$'> word
   key = word
   value = word
   <sentNotOr> = text | variable | choices | ranges | group
   sentence = ((sentNotOr | orr) {space} )+
   choices = <'['> {space} sentence {space} <']'>
   ranges = variable <'<'> {space} number [comma number] {space} <'>'>
   orr = sentNotOr {space} (<'|'> {space} sentNotOr {space})+
   group = <'('> sentence <')'>

   text = #'[0-9A-Za-z\\p{script=Han}]+'
   <number> = #'\\d+'
   <word> = #'\\w+'
   <semicolon> = <#'\\s*;\\s*'>
   <comma> = <#'\\s*,\\s*'>
   <space> = <#'\\s+'>")

(defn grammar-keyvalue
  "根据自定义YBNF语法树获取到YBNF关键字与值"
  [grammar-tree]
  (let [[_ & args] (last grammar-tree)]
    (into {} (filter (fn [item] (let [[_ v] item] v))
      (map (fn [arg]
        (let [[_ vari _] arg
              [_ [_ vn] & kv] vari]
          [(keyword vn) (if kv (into {} kv) nil)])) args)))))

(def grammar-compile-memoize
  (memoize (fn [lang] (insta/parser lang))))

(def grammar-compile-parser
  (grammar-compile-memoize grammar-lang))

(defn grammar-compile
  ([^String lang]
    (insta/parse grammar-compile-parser lang))
  ([^String grammar ^String lang]
    (insta/parse (grammar-compile-memoize grammar) lang)))

(defn grammar-parser
  "根据自定义YBNF语法树转换成Clojure BNF规则"
  [grammar-tree]
  (insta/transform {
    :root (fn [& args]
      (condp = (.size args)
        2 (let [[h b] args] [h nil b])
        3 (let [[h x b] args] (if (string? x) [h nil (str x "\n" b)] [h x b]))
        4 (let [[h c m b] args] [h c (str m "\n" b)])))
    :head (fn [& args] args)
    :callable (fn [& args]
      (loop [[[_ & params] & calls] args
             result (java.util.ArrayList.)]
        (if (nil? params) result
          (recur calls (doto result (.add (.toArray params)))))))
    :include (fn [args] args)
    :main (fn [marg] (str "root = " marg))
    :text (fn [txt] (str "'" txt "'"))
    :variable (fn [vari] vari)
    :varname (fn [arg & args] (if (empty? args) (str "<" arg ">") arg))
    :choices (fn [sent] (str "[" sent "]"))
    :group (fn [sent] (str "(" sent ")"))
    :ranges (fn [& args]
      (let [[vari start end] args
            s (Integer/parseInt start)
            e (if end (Integer/parseInt end) 0)]
        (str (cs/join "" (map (fn [x] (str "(" vari ")")) (range s)))
          (cs/join "" (map (fn [x] (str "[" vari "]")) (range s e))))))
    :orr (fn [& args] (cs/join "|" args))
    :sentence (fn [& args] (cs/join " " args))
    :define (fn [& args] (let [[vn vv] args] (str vn " = " vv)))
    :body (fn [& args] (cs/join "\n" args))} grammar-tree))

(defn ybnf-sentence
  [args]
  (let [[_ & arg] args]
    (loop [[word & sent] args result ""]
      (if (nil? word) result
        (recur sent (cond
          (keyword? word) result
          (sequential? word) (str result (ybnf-sentence word))
          :else (str result word)))))))

(defn ybnf-keyword
  [args]
  (let [[_ & arg] args]
    (loop [[it & its] (if (= (.size arg) 1) (first arg) arg) result []]
      (if (nil? it) (into {} result)
        (recur (or its []) (cond
          (keyword? it) (conj result [it (if (= (.size its) 1) (first its) its)])
          (sequential? it) (apply conj result (ybnf-keyword [:root it]))
          :else result))))))

(defn ybnf-parser
  [ybnf-result keyvalues]
  (let [objects (java.util.HashMap.) slots (java.util.HashMap.)]
    (loop [[k & ks] (keys ybnf-result)]
      (if (nil? k) [objects slots]
        (let [{nk :key nv :value} (k keyvalues)]
          (if (nil? nv)
            (.put objects nk (ybnf-sentence (k ybnf-result)))
            (.put slots nk nv)) (recur ks))))))



