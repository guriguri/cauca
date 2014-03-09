(ns cauca.util
  (:require [clojure.string :as string]
            )
  )

(defmacro load-resource [ref-var & init-fn]
  `(dosync 
     (when (nil? @~ref-var))
       (~@init-fn)))

(deftype Foo [])
(defn get-url [path] (.getResource (.getClass (Foo.)) path))

(defn validation [value min max regex isMatches]
  (if (nil? value)
    false
    (if (string? value)
      (if (and (= (nil? min) false) (< (count value) min))
        false
        (if (and (= (nil? max) false) (> (count value) max))
          false
          (if (= (nil? regex) false)
            (let [groups (re-matches regex value)]
              (if (= (nil? groups) isMatches) false true))
            )
          )
        )
      (if (and (= (nil? min) false) (< value min))
        false
        (if (and (= (nil? max) false) (> value max)) false true)
        )
      )
    )
  )

(defn get-locale [request]
  (let [locale (ref nil)]
    (if (and (= (nil? request) false) (= (nil? (first request)) false))
      (dosync
        (doseq [[lang, country, variant]
                (map #(. % split "_")
                     (map #(string/replace % #"-" "_")
                          (. (((first request) :headers) "accept-language") split ",; ")))]
          (if (nil? country)
            (ref-set locale (new java.util.Locale lang))
            (if (nil? variant)
              (ref-set locale (new java.util.Locale lang country))
              (ref-set locale (new java.util.Locale lang country variant))
              )
            )
          )
        )
      )
    (if (nil? @locale)
      (java.util.Locale/getDefault)
      @locale)
    )
  ) 