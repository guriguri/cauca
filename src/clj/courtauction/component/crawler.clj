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
  (let [addr (first (:content (nth addr-info 1)))
        area (last addr-info)]
    (map #(string/trim (string/replace % #"\n" " ")) (seq [addr area]))
    )
  )

(defn get-auction-info [auction-info]
  (let [auction-agent (first auction-info)
        info (re-seq #"'[^']+'" (:onclick (:attrs (second auction-info))))
        auction-tel (first info)
        auction-date (second info)
        auction-salesroom (nth info 2)]
    (map #(string/trim (string/replace % #"'" ""))
         (seq [auction-agent auction-tel auction-date auction-salesroom]))
    )
  )

(defn set-courtauction [cols]
  (struct courtauction
          ; :id
          (:content (first (e/select-nodes* (:content (nth cols 1))
                                            [(e/tag= :b)]))) 
          
          ; :type-info
          (map #(string/trim %) (filter #(= (map? %) false)
                                        (:content (nth cols 2))))
          
          ; :addr-info
          (get-addr-info (:content
                           (first (e/select-nodes*
                                    (:content (nth cols 3)) [(e/tag= :div)]))))
          
          ; :remarks
          (map #(string/trim (string/replace % #"\n" " "))
               (:content (nth cols 4)))
          
          ; :value-info
          (map #(string/trim (first (:content %)))
               (e/select-nodes* (:content (nth cols 5)) [(e/tag= :div)]))

          ; :auction-info
          (get-auction-info (:content
                              (first (e/select-nodes*
                                       (:content (nth cols 6))
                                       [(e/tag= :div)]))))
          
          ; :status
          (map #(string/trim %)
               (filter #(= (map? %) false) (:content (nth cols 7))))
          )
  )

(defn courtauction-parser [html]
  (let [nodes (e/html-snippet html)
        courtauctions (ref [])]
    (doseq [rows (e/select-nodes* nodes [(e/attr= :class "Ltbl_list")
                                         (e/tag= :tbody)
                                         (e/tag= :tr)])]
      (dosync (alter courtauctions conj
                     (set-courtauction
                       (e/select-nodes* (:content rows) [(e/tag= :td)]))))
      )
    courtauctions
    )
  )

(defn sigu-parser [xml]
  (let [nodes (e/html-snippet xml)
        sigus (ref [])]
    (doseq [row (filter #(not-empty (:value (:attrs %)))
                        (e/select-nodes* nodes [(e/tag= :option)]))]
      (dosync (alter sigus conj
                     (struct sigu (:value (:attrs row))
                             (first (map #(string/trim %) (:content row))))))
      )
    sigus
    )
  )

(defn get-sigu-list! [sido]
  (let [resp (client/post
               "https://www.courtauction.go.kr/RetrieveAucSigu.ajax"
               {
                :as "euc-kr"
                :headers { "Host" "www.courtauction.go.kr"
                  "User-Agent" "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.8; ko; rv:1.9.0.14) Gecko/2009082706 Firefox/3.0.14"
                  "Accept" "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
                  "Accept-Language" "ko-KR,ko;q=0.8,en-US;q=0.5,en;q=0.3"
                  "Accept-Charset" "windows-949,utf-8;q=0.7,*;q=0.7"
                  }
                :form-params {
                              :sidoCode sido
                              :id2 "idSiguCode"
                              :id3 "idDongCode"
                              }
                }
               ) ]
    (sigu-parser (:body resp))
    )
  )

(defn get-auction-list! [sido sigu page-size]
  (let [all-courtauctions (ref [])
        courtauctions (ref nil)
        resp (ref nil)]
    (loop [target-row 1]
      (dosync
        (ref-set resp (client/post "https://www.courtauction.go.kr/RetrieveRealEstMulDetailList.laf"
                              {:as "euc-kr"
                               :headers { "Host" "www.courtauction.go.kr"
                                         "User-Agent" "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.8; ko; rv:1.9.0.14) Gecko/2009082706 Firefox/3.0.14"
                                         "Accept" "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
                                         "Accept-Language" "ko-KR,ko;q=0.8,en-US;q=0.5,en;q=0.3"
                                         "Accept-Charset" "windows-949,utf-8;q=0.7,*;q=0.7"
                                         }
                               :form-params {
                                             :_FORM_YN	"Y"
                                             :bubwLocGubun "2"
                                             :daepyoSidoCd sido
                                             :daepyoSiguCd sigu
                                             :mDaepyoSidoCd sido
                                             :mDaepyoSiguCd sigu
                                             :srnID "PNO102000"
                                             :targetRow target-row
                                             }
                               })
                 )
        (ref-set courtauctions @(courtauction-parser (:body @resp)))
        (alter all-courtauctions into @courtauctions)
        )
      (if (= (count @courtauctions) page-size)
        (recur (+ target-row page-size))
        all-courtauctions
        )
      )
    ) 
  )

(defn -main
  ([]
  (log/configure-logback "/courtauction-logback.xml")
  (config/config-yaml "/application-context.yaml") 
  (doseq [sido (config/get-value :location.SidoCd)]
    (let [sido-code (first (. sido split ","))]
      (doseq [sigu @(get-sigu-list! sido-code)]
;        (log/log-message "sido=" sido ", sigu=" sigu ", rows.count="
        (println "sido=" sido ", sigu=" sigu ", rows.count="
                 (count @(get-auction-list! sido-code (:id sigu) 20)))
        )
      )
    )
  )
  )