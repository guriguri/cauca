(ns cauca.component.crawler
  (:use [cauca.domain]
    )
  (:require [cauca.factory :as f]
            [cauca.log :as log]
            [cauca.config :as config]
            [clj-http.client :as client]
            [net.cgrand.enlive-html :as e] 
            [clojure.string :as string]
            )
  (:gen-class)
  )

(defn get-value[coll key]
  (if (map? coll)
    (key coll)
    coll
    )
  )

(defn trim[data idx]
  (if (nil? data)
    data
    (if (seq? data)
      (string/trim (nth data idx))
      (string/trim data))
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
  (let [caInfo (map #(trim % 0)
                    (filter #(not-empty (trim % 0))
                            (map #(get-value % :content)
                                 (:content (first (:content (nth cols 1)))))))
        itemInfo (map #(string/trim %)
                      (filter #(= (map? %) false) (:content (nth cols 2))))
        addrInfo (get-addr-info
                   (:content (first
                               (e/select-nodes*
                                 (:content (nth cols 3)) [(e/tag= :div)]))))
        addrs (map #(string/trim %)
                   (filter #(not-empty %) (. (first addrInfo) split " ")))
        remarks (map #(string/trim
                        (string/replace % #"\n" " ")) (:content (nth cols 4)))
        valueInfo (map #(string/trim (first (:content %)))
                       (e/select-nodes* (:content (nth cols 5)) [(e/tag= :div)]))
        auctionInfo (get-auction-info
                      (:content (first
                                  (e/select-nodes*
                                    (:content (nth cols 6)) [(e/tag= :div)]))))
        status (map #(string/trim %)
                    (filter #(= (map? %) false) (:content (nth cols 7))))
        now (new java.util.Date)
        ]
    (struct courtauction
            nil 
            (nth caInfo 0) ;:court
            (nth caInfo 1) ;:caNo
            (string/join " " caInfo) ;:caDesc
            (nth itemInfo 0) ;:itemNo
            (nth itemInfo 1) ;:itemType
            (nth addrs 0) ;:addr0
            (nth addrs 1) ;:addr1
            (nth addrs 2) ;:addr2
            (string/join " " addrs) ;:addr
            (nth addrInfo 1) ;:addrInfo
            (nth remarks 0);:remarks
            (string/replace (nth valueInfo 0) "," "") ;:value
            (string/replace (nth valueInfo 1) "," "") ;:valueMin
            (nth auctionInfo 0) ;:auctionInfo
            (nth auctionInfo 1) ;:auctionTel
            (nth auctionInfo 2) ;:auctionDate
            (nth auctionInfo 3) ;:auctionLoc
            (nth status 0) ;:status
            now ;:regDate
            now ;:updDate
            )
    )
  )

(defn courtauction-parser [html]
  (let [nodes (e/html-snippet html)
        courtauctions (ref [])]
    (doseq [rows (e/select-nodes* nodes [(e/attr= :class "Ltbl_list")
                                         (e/tag= :tbody)
                                         (e/tag= :tr)])]
      (try 
        (dosync (alter courtauctions conj
                       (set-courtauction
                         (e/select-nodes* (:content rows) [(e/tag= :td)]))))
        (catch Exception e
          (log/log-error e (e/select-nodes* (:content rows) [(e/tag= :td)]))
          )
        )
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

(defn add-courtauctions! [dao-impl# sido page-size]
  (let [sido-code (first (. sido split ","))
        courtauctions (ref nil)]
    (doseq [sigu @(get-sigu-list! sido-code)]
      (dosync
        (ref-set courtauctions
                 @(get-auction-list! sido-code (:id sigu) page-size))
        )
      (doseq [courtauction @courtauctions]
        (try
          (.add-courtauction dao-impl# courtauction)
          (catch Exception e 
            (if (or
                  (= (instance? com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException e) false)
                  (not= (.getErrorCode e) 1062))
              (log/log-error e courtauction)
              )
            )
          )
        )
      (log/log-message "sido=" sido ", sigu=" sigu
                       ", rows.count=" (count @courtauctions))
      (Thread/sleep 1000)
      )
    )
  )

(defn -launch
  [repeatCnt sleepMsec]
  (log/configure-logback "/cauca-logback.xml")
  (config/config-yaml "/cauca-context.yaml") 
  (log/log-message "START crawler!!!, repeatCnt=" repeatCnt ", sleepMsec=" sleepMsec)
  (loop [dao-impl# (f/get-obj :courtauction-dao) cnt 1]
    (doseq [sido (config/get-value :location.SidoCd)]
      (add-courtauctions! dao-impl# sido 20)
      )
    (log/log-message "currentCnt=" cnt)
    (if (or (= repeatCnt -1) (< cnt repeatCnt))
      (do 
        (Thread/sleep sleepMsec)
        (recur (f/get-obj :courtauction-dao) (inc cnt))
        )
      )
    )
  (log/log-message "STOP crawler!!!")
  )

(defn -main
  [& args]
  (let [argsCnt (count args)
        repeatCnt (ref 1)
        sleepMsec (ref 1000)]
    (log/log-message "\n\nUsage: crawler [REPEAT COUNT] [SLEEP SECOND]\n  REPEAT COUNT: -1(infinite), 1, 2, ...\n  SLEEP SECOND: Repeat the cycle\n")
    (dosync
      (if (= argsCnt 1)
        (ref-set repeatCnt (Integer/parseInt (first args)))
        (if (>= argsCnt 2)
          (do
            (ref-set repeatCnt (Integer/parseInt (first args)))
            (ref-set sleepMsec (* (Integer/parseInt (second args)) 1000))
            )
          )
        )
      )
    (if (= repeatCnt -1)
      (.start (Thread. -launch repeatCnt sleepMsec))
      (-launch @repeatCnt @sleepMsec)
      )
    )
  )