(ns courtauction.dao.mysql-dao
  (:use [courtauction.api.dao]
        [courtauction.domain]
        )
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.java.jdbc.sql :as sql]
            )
 )

(defn mysql-courtauction-dao [db] 
  (reify 
    courtauction-dao  
    (add-courtauction [this courtauction]
      (jdbc/insert! db :courtauction { :caNo (:caNo courtauction)
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
                                      }
                    )
      )
    (get-courtauction [this id]
      (jdbc/query db
                  (sql/select * :courtauction (sql/where {:id id}))
                  :identifiers str
                  )
      )
    )
  )