(ns cauca.dao.mysql-dao
  (:use [cauca.api.dao]
        [cauca.domain]
        )
  (:require [cauca.db :as db-pool]
            [clojure.java.jdbc :as jdbc]
            [clojure.java.jdbc.sql :as sql]
            )
 )

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
      (jdbc/query (db-pool/connection)
                  ["select * from courtauction limit 10"]
                  :identifiers str
                  )
      )
    )
  )