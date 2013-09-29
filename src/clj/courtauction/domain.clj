(ns courtauction.domain)
 
(defstruct sigu :id :description)

(defstruct courtauction :id :type-info :addr-info :remarks :value-info :auction-info :status)