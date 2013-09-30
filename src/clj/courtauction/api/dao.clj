(ns courtauction.api.dao
  (:use [courtauction.domain])
 )

(defprotocol courtauction-dao
  (add-courtauction [this courtauction])
  (get-courtauction [this id])
)