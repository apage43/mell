(ns mell.util
  (:require clojure.walk))

(defn- dotify-collapse-1 [m]
  (if (map? m)
    (into {} (mapcat (fn [[ok ov]]
                       (if (map? ov)
                         (map (fn [[ik iv]] [(str (name ok) "." (name ik)) iv]) ov)
                         [[ok ov]])) m))m))

(defn dotify-collapse [m]
  (clojure.walk/postwalk dotify-collapse-1 m))

(defn to-props [m]
  (doto (java.util.Properties.)
    (.putAll (dotify-collapse m))))

