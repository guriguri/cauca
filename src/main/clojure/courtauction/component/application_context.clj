(ns courtauction.component.application-context 
  (:use [courtauction.util]) 
  (:require [courtauction.log :as log]
            [courtauction.config :as config])
  )

(def app-cxt-map (ref nil))
(def memcache-server "127.0.0.1")

(defn- application-context []  
  (log/configure-logback "/courtauction-logback.xml")
  (config/config-yaml "/application-context.yaml")
  
;  (def mc (net.spy.memcached.MemcachedClient. (list (java.net.InetSocketAddress. memcache-server 11211))))
;  (let [user-dao (dao/->MemcacheUserDao mc)
;        book-dao (dao/memcache-book-dao mc)
;        user-reading-book-dao (dao/->MemcacheUserReadingBookDao mc)
;        reading-list-service (service/->ReadingListServiceImpl user-dao book-dao user-reading-book-dao)
;        add (fn [name obj] (alter app-cxt-map assoc name obj))]      
;    (dosync
;	    (ref-set app-cxt-map {})
;	    (add :user-dao user-dao)
;		  (add :book-dao book-dao)
;		  (add :user-reading-book-dao user-reading-book-dao)
;		  (add :reading-list-service reading-list-service))
;  )
)

(defn get-obj [obj-name] 
  (load-resource app-cxt-map application-context)
  (dosync (get @app-cxt-map obj-name))
  )