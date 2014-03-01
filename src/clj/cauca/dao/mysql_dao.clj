(ns cauca.dao.mysql-dao
  (:use [cauca.api.dao]
        [cauca.domain]
        )
  (:require [cauca.db :as db-pool]
            [clojure.java.jdbc :as jdbc]
            [clojure.java.jdbc.sql :as sql]
            [clojure.string :as string]
            )
 )

(defn get-query-str [params]
  (let [getNum (fn [data default] (if (nil? data) default (if (string? data) (Integer/parseInt data) data)))
        getVal (fn [data min max] (if (< data min) min (if (> data max) max data)))
        page (getVal (getNum (params "page") 1) 1 100) 
        pageSize (getVal (getNum (params "pageSize") 10) 0 100)]
    (string/join \newline
                 ["SELECT * FROM courtauction"
                  "WHERE 1 = 1"
                  (string/join
                    \newline
                    (map #(str "AND " (key %) " = '" (val %) "'")
                         (filter #(contains? #{"itemType" "addr0" "addr1"} (key %)) params)))
                  (if-not (nil? (params "minValue")) (str "AND valueMin >= " (params "minValue"))) 
                  (if-not (nil? (params "maxValue")) (str "AND valueMin <= " (params "maxValue"))) 
                  (if-not (nil? (params "auctionStartDate")) (str "AND auctionDate >= '" (params "auctionStartDate") "'")) 
                  (if-not (nil? (params "auctionEndDate")) (str "AND auctionDate <= '" (params "auctionEndDate") "'")) 
                  "ORDER BY id DESC"
                  (str "LIMIT " (* (- page 1) pageSize) ", " pageSize)])))

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