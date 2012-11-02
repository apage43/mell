(defproject mell "0.1.0-SNAPSHOT"
  :description "javamail stuff"
  :url "http://github.com/apage43/mell"
  :license {:name "WTFPL"
            :url "http://sam.zoy.org/wtfpl/"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [clojure-lanterna "0.9.2"]
                 [crouton "0.1.1"]
                 [environ "0.3.0"]
                 [me.raynes/conch "0.4.0"]
                 [hiccup "1.0.1"]
                 [javax.mail/mail "1.4.5"] ]
  :main mell.core)
