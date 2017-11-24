(ns ybnf.core
  (:import ybnf.compiler)
  (:require [ybnf.grammar :as gm]
            [clojure.pprint :as pp]))

(def lang-test
  "#YBNF 0.1 utf8;
   #include sss.ybnf;
   service common;
   callable @cutword(title), @cutword(poetry);
   root $main;
   $main = $poetry_main;

   $data = 静夜诗|赠汪伦|登黄鹤楼|hello|world;
   $poetry_main{service%poetry} = [我想听] $data;
")

(def ttt "我想听赠汪伦")

(defn -main
  [& args]
  (let [cp (ybnf.compiler. lang-test)]
;    (pp/pprint (.getFailure cp))
    (println (.getGrammar cp))
    (println (.getCallable cp))
;    (pp/pprint (.get (.state cp) "tree"))
    (pp/pprint (.getKeyValue cp))
    (pp/pprint (.execCompile cp ttt))
  ))


