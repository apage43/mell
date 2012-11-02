(ns mell.core
  (:use clojure.pprint
        mell.util)
  (:require [clojure.string :as s]
            [mell.compose :refer [compose]]
            [mell.mail :as mail])
  (:gen-class))

(defn idp [t] (pr t (keys (bean t))) t)

(defn load-config []
  (let [mtext (or (slurp (str (get (System/getenv) "HOME") "/.mellrc")) "{}")]
    (read-string mtext)))

(defn -main
  [cmd & args]
  (let [config (load-config)
        session (mail/session config)
        store (mail/connect session)]
    (case cmd
      "compose" (mail/send-msg (compose session args))
      "draft" (mail/insert (mail/folder (mail/connect session)
                                        "[Gmail]/Drafts")
                           (compose session args)))
    (System/exit 0)))
