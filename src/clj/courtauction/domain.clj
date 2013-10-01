(ns courtauction.domain)
 
(defstruct sigu :id :description)

(defstruct courtauction
  :id :court :caNo :caDesc :itemNo
  :itemType :addr0 :addr1 :addr2 :addr
  :addrInfo :remarks :value :valueMin :auctionInfo
  :auctionTel :auctionDate :auctionLoc :status :regDate
  :updDate)