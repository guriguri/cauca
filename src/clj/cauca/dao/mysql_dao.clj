(ns cauca.dao.mysql-dao
  (:use [cauca.api.dao]
        [cauca.domain]
        )
  (:require [cauca.db :as db-pool]
            [clojure.java.jdbc :as jdbc]
            [clojure.java.jdbc.sql :as sql]
            )
 )

(defn get-query-str [params]
  (let [getNum (fn [data default] (if (nil? data) default (if (string? data) (Integer/parseInt data) data)))
        getVal (fn [data min max] (if (< data min) min (if (> data max) max data)))
        page (getVal (getNum (params "page") 1) 1 100) 
        pageSize (getVal (getNum (params "pageSize") 10) 0 100)]
    (str "select * from courtauction where 1 = 1 "
         (apply str (map #(str "and " (key %) " = '" (val %) "' ")
                         (filter #(= (contains? #{"page" "pageSize"} (key %)) false) params)))
         "limit " (* (- page 1) pageSize) ", " pageSize)))

(defn mysql-courtauction-dao [] 
  (reify 
    courtauction-dao  
    (add-courtauction [this courtauction]
      (jdbc/insert! (db-pool/connection) :courtauction {:court (:court courtauction)
                                      :caNo (:caNo courtauction)
                                      :caDesc (:caDesc courtauction)
                                      :itemNo (:itemNo courtauction)
                                      :itemType (:itemType courtauction)
                                      :addr0 (:addr0 courtauction)
                                      :addr1 (:addr1 courtauction)
                                      :addr2 (:addr2 courtauction)
                                      :addr (:addr courtauction)
                                      :addrInfo (:addrInfo courtauction)
                                      :remarks (:remarks courtauction)
                                      :value (:value courtauction)
                                      :valueMin (:valueMin courtauction)
                                      :auctionInfo (:auctionInfo courtauction)
                                      :auctionTel (:auctionTel courtauction)
                                      :auctionDate (:auctionDate courtauction)
                                      :auctionLoc (:auctionLoc courtauction)
                                      :status (:status courtauction)
                                      :regDate (:regDate courtauction)
                                      :updDate (:updDate courtauction)
                                      }
                    )
      )
    (get-courtauction [this id]
      (jdbc/query (db-pool/connection)
                  (sql/select * :courtauction (sql/where {:id id}))
                  :identifiers str
                  )
      )
    (get-courtauction-list [this params]
      (println (get-query-str params))
      (jdbc/query (db-pool/connection)
                  [(get-query-str params)]
                  :identifiers str
                  )
      )
    )
  )