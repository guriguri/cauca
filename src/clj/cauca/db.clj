(ns cauca.db
  (:require [cauca.config :as config]
            )
  (:import [com.mchange.v2.c3p0 ComboPooledDataSource]
           )
  )

(defn pooled-data-source []
  (config/config-yaml "/cauca-context.yaml")
  (let [db {:classname (config/get-value :db.classname)
            :subprotocol (config/get-value :db.subprotocol)
            :subname (config/get-value :db.subname)
            :user (config/get-value :db.user)
            :password (config/get-value :db.password)
            :db.max.idle.time.excess.connection.sec
            (config/get-value :db.max.idle.time.excess.connection.sec)
            :db.max.idel.time.sec (config/get-value :db.max.idel.time.sec) }
        datasource (ComboPooledDataSource.)]
    (.setDriverClass datasource (:classname db))
    (.setJdbcUrl datasource (str "jdbc:" (:subprotocol db) ":" (:subname db)))
    (.setUser datasource (:user db))
    (.setPassword datasource (:password db))
    (.setMaxIdleTimeExcessConnections datasource
      (:db.max.idle.time.excess.connection.sec db))
    (.setMaxIdleTime datasource (:db.max.idel.time.sec db))
    {:datasource datasource}
    )
  )

(def connection-pool
  (delay (pooled-data-source))
  )

(defn connection [] @connection-pool)