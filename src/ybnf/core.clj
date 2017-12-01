(ns ybnf.core
  (:import ybnf.compiler)
  (:require [ybnf.grammar :as gm]
            [instaparse.core :as insta]
            [clojure.pprint :as pp]))

(def lang-test
  "#YBNF 0.1 utf8;
   #include sss.ybnf;
   service common;
   callable @cutword(title), @cutword(poetry);
   root $main;
   $main = $poetry_main;

   $data{title} = 静夜诗|赠汪伦|登黄鹤楼;
   $poetry_main{service%poetry} = {$ch} 我想听 $data {$ch};
")

(def ttt "真的我想听赠汪伦是真的")

(defn -main
  [& args]
  (let [cp (ybnf.compiler. lang-test)]
;    (pp/pprint (.getFailure cp))
    (def grammar (str (.getGrammar cp) "\n<ch> = #'[0-9A-Za-z\\p{script=Han}]'"))
    (println grammar)
;    (pp/pprint (.get (.state cp) "tree"))
;    (pp/pprint (.getKeyValue cp))
;    (pp/pprint (.execCompile cp ttt))
    (def tree (insta/parse (insta/parser grammar) ttt))
    (pp/pprint tree)
  )
)


