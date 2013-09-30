(ns courtauction.beans
  (:use [courtauction.util]) 
  (:require [courtauction.log :as log]
            [courtauction.config :as config]
            [courtauction.dao.mysql-dao :as dao]
            )
  )

(def beans-map (ref nil))

(defn- beans []  
  (log/configure-logback "/courtauction-logback.xml")
  (config/config-yaml "/application-context.yaml")
  (let [db { :subprotocol (config/get-value :db.subprotocol)
            :subname (config/get-value :db.subname)
            :user (config/get-value :db.user)
            :password (config/get-value :db.password) }
        courtauction-dao (dao/mysql-courtauction-dao db)
        add (fn [key obj] (alter beans-map assoc key obj))]      
    (dosync
      (ref-set beans-map {})
      (add :courtauction-dao courtauction-dao))
    )
  )

(defn get-obj [key] 
  (load-resource beans-map beans)
  (dosync (get @beans-map key))
  )