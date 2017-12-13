(defproject ybnf-compiler "0.1.0-SNAPSHOT"
  :description "FIXME: YBNF Compiler"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main ybnf.core
  :aot [ybnf.compiler ybnf.jutils]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [instaparse "1.4.8"]])
