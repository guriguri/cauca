(ns courtauction.component.crawler
  (:use [courtauction.domain]
    )
  (:require [courtauction.beans :as beans]
            [courtauction.log :as log]
            [courtauction.config :as config]
            [clj-http.client :as client]
            [net.cgrand.enlive-html :as e] 
            [clojure.string :as string]
            )
  )

(defn get-addr-info [addr-info]
  (def addr (first (:content (nth addr-info 1))))
  (def area (last addr-info))
  (map #(string/trim (string/replace % #"\n" " ")) (seq [addr area]))
  )


(defn set-courtauction [cols]
  (struct courtauction
          (:content (first (e/select-nodes* (:content (nth cols 1)) [(e/tag= :b)]))) ; :id
          (map #(string/trim %) (filter #(= (map? %) false) (:content (nth cols 2)))) ; :type-info 
          (get-addr-info (:content (first (e/select-nodes* (:content (nth cols 3)) [(e/tag= :div)])))) ; :addr-info
          (map #(string/trim %) (:content (nth cols 4))) ; :remarks
;          (e/select-nodes* (:content (nth cols 5)) [(e/tag= :div)]); :value
          (map #(:content %) (e/select-nodes* (:content (nth cols 5)) [(e/tag= :div)])); :value
          
          (nth cols 6) ; :auction-info
          (nth cols 7) ; :status
          )
  )

(defn courtauction-parser [html]
  (def nodes (e/html-snippet html))
  (doseq [rows (e/select-nodes* nodes [
                                           (e/attr= :class "Ltbl_list")
                                           (e/tag= :tbody)
                                           (e/tag= :tr)])]
    (def cols (e/select-nodes* (:content rows) [(e/tag= :td)]))
    (def a-courtauction (set-courtauction cols))
    (println "-----")
    (println (:value a-courtauction))
    )
  )

(defn get-list! [sido sigu]
  (do
    (def resp (client/post "https://www.courtauction.go.kr/RetrieveRealEstMulDetailList.laf"
                           {:as "euc-kr"
                            :headers {
                                      "Host" "www.courtauction.go.kr"
                                      "User-Agent" "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.8; ko; rv:1.9.0.14) Gecko/2009082706 Firefox/3.0.14"
                                                 "Accept" "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
                                       "Accept-Language" "ko-KR,ko;q=0.8,en-US;q=0.5,en;q=0.3"
                                       "Accept-Charset" "windows-949,utf-8;q=0.7,*;q=0.7"
                                       }
                            :form-params {
                                          :_CUR_CMD	"RetrieveMainInfo.laf"
                                          :_CUR_SRNID	"PNO102000"
                                          :_FORM_YN	"Y"
                                          :_NEXT_CMD "RetrieveRealEstMulDetailList.laf"
                                          :_NEXT_SRNID "PNO102002"
                                          :_PRE_SRNID "PNO102001"
                                          :_SRCH_SRNID "PNO102000"
                                          :bubwLocGubun "2"
                                          :daepyoSidoCd sido
                                          :daepyoSiguCd sigu
                                          :mDaepyoSidoCd sido
                                          :mDaepyoSiguCd sigu
                                          :mvRealGbncd "1"
                                          :notifyLoc "1"
                                          :notifyNewLoc "1"
                                          :notifyRealRoad "1"
                                          :realVowel "00000_55203"
                                          :srnID "PNO102000"
                                          :vowelSel "00000_55203" 
                                          }
                            }
                              )
      )
    (courtauction-parser (:body resp)) 
    )
  ) 

(defn -main
  ([]
  (log/configure-logback "/courtauction-logback.xml")
  (config/config-yaml "/application-context.yaml") 
  (def sido (first (. (config/get-value :location.SidoCd) split ",")))
  (doseq [tSigu (config/get-value :location.SiguCd)]
    (def sigu (first (. tSigu split ",")))
    (get-list! sido sigu)
    )
  )
  )