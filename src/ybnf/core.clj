(ns ybnf.core
  (:import ybnf.compiler)
  (:require [ybnf.grammar :as gm]
            [clojure.pprint :as pp]))

(def lang-test
  "#YBNF 0.1 utf8;
   service video;
   root $main;
   $poem{action%poem} = 诗;
   $ci{action%ci} = 词;
   $song{action%song} = 歌;
   $main{video} = 来首 ($poem|$ci|$song);")

(def ttt "来首歌")

(defn -main
  [& args]
  (let [cp (ybnf.compiler. lang-test)]
;    (pp/pprint (.getHeader cp))
;    (println (.getGrammar cp))
;    (pp/pprint (.get (.state cp) "tree"))
;    (pp/pprint (.getKeyValue cp))
    (pp/pprint (.execCompile cp ttt))
  ))


