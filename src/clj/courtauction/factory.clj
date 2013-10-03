(ns courtauction.factory
  (:use [courtauction.util]) 
  (:require [courtauction.log :as log]
            [courtauction.config :as config]
            [courtauction.db :as db]
            [courtauction.dao.mysql-dao :as dao]
            )
  )

(def beans-map (ref nil))

(defn- beans []  
  (log/configure-logback "/courtauction-logback.xml")
  (config/config-yaml "/application-context.yaml")
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