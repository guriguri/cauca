(ns cauca.factory
  (:use [cauca.util]) 
  (:require [cauca.log :as log]
            [cauca.config :as config]
            [cauca.db :as db]
            [cauca.dao.mysql-dao :as dao]
            )
  )

(def beans-map (ref nil))

(defn- beans []  
  (log/configure-logback "/cauca-logback.xml")
  (config/config-yaml "/cauca-context.yaml")
  (let [courtauction-dao (dao/mysql-courtauction-dao)
        add (fn [key obj] (alter beans-map assoc key obj))]      
    (dosync
      (ref-set beans-map {})
      (add :courtauction-dao courtauction-dao))
    )
  )

(defn get-obj [key] 
  (load-resource beans-map beans)
  (dosync
    (get @beans-map key)
    )
  )