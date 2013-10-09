(ns cauca.util
  )

(defmacro load-resource [ref-var & init-fn]
  `(dosync 
     (when (nil? @~ref-var))
       (~@init-fn)))

(deftype Foo [])
(defn get-url [path] (.getResource (.getClass (Foo.)) path))