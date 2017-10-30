(ns ybnf.core
  (:import ybnf.compiler)
  (:require [ybnf.grammar :as gm]
            [clojure.pprint :as pp]))

(def lang-test
  "#YBNF 0.1 utf8;
   service common;
   root $main;
   $main = $poetry_main|$song_main|$xiangsheng_main;

   $character = A|B|C|D|E|F|G|H|I|J|K|M|N|L|O|P|Q|R|S|T|U|V|W|X|Y|Z|a|b|c|d|e|f|g|h|i|j|k|m|l|n|o|p|q|r|s|t|u|v|w|x|y|z;
   $_yyd_ch_ = $_yyd_han_|$character;
   $han_digital = 零|一|二|三|四|五|六|七|八|九|十|百|千|万;
   $digital = $_yyd_digital_|$han_digital;

   $author{author} = $_yyd_ch_<1,6>;
   $shi{action%shi} = 诗;
   $ci{action%ci} = 词;
   $poetry_main{service%poetry} = $author 的 ($shi|$ci);

   $songer{songer} = $_yyd_ch_<1,6>;
   $song{action%song} = 歌;
   $song_main{service%song} = $songer 的 $song;

   $yanyuan{yanyuan} = $_yyd_ch_<1,6>;
   $xiangsheng{action%xiangsheng} = 相声;
   $xiangsheng_main{service%xiangsheng} = $yanyuan 的 $xiangsheng;
")

(def ttt "李白的诗")

(defn -main
  [& args]
  (let [cp (ybnf.compiler. lang-test)]
    (.mergeGrammar cp "<_yyd_han_> = #'\\p{script=Han}'\n<_yyd_digital_> = #'\\d'\n<_yyd_num_> = _yyd_digital_+")
;    (pp/pprint (.getFailure cp))
    (println (.getGrammar cp))
;    (pp/pprint (.get (.state cp) "tree"))
    (pp/pprint (.getKeyValue cp))
    (pp/pprint (.execCompile cp ttt))
  ))


