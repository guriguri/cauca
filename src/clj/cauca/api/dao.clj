(ns cauca.api.dao
  (:use [cauca.domain])
 )

(defprotocol courtauction-dao
  (add-courtauction [this courtauction])
  (get-courtauction [this id])
)