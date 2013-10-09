(ns cauca.config
  (:use [clojure.java.io]
        [clj-yaml.core]
        [cauca.util]))

(def yaml (ref nil))

(defn- read-yaml [path]
  (def url (get-url path))
  (def sb (StringBuilder.))
  (with-open [rdr (reader url)]
	  (doseq [line (line-seq rdr)]
	    (-> sb 
          (.append line) 
          (.append \newline))
     )
   )
  (dosync    
    (ref-set yaml (parse-string (.toString sb)))
    )
  )

(defn config-yaml [path]
  (load-resource yaml read-yaml path))

(defn get-value [key]
  (when (nil? @yaml)
    (throw (IllegalAccessError. "Yaml Configuration Not Yet Loaded...")))
  (let [parsed @yaml]
    (parsed key)
    )
  )