(ns courtauction.domain)
 
(defstruct sigu :id :description)

(defstruct courtauction
  :id :caNo :caDesc :itemNo :itemType
  :addr0 :addr1 :addr2 :addr3 :addrInfo
  :remarks :value :valueMin :auctionInfo :auctionTel
  :auctionDate :auctionLoc :status)