(ns courtauction.component.crawler
  (:require [courtauction.beans :as beans]
            [courtauction.log :as log]
            [courtauction.config :as config]
            [clj-http.client :as client]
            [net.cgrand.enlive-html :as e] 
            )
  )

(defn getbyitemprop
  "Extract node content from HTML"
  [html key value]
  (e/select-nodes* (e/html-snippet html)
                   [(e/attr= key value)]))

(defn get-list! [sido sigu]
  (do
    (def resp (client/post "https://www.courtauction.go.kr/RetrieveRealEstMulDetailList.laf"
                           {:as "euc-kr"
                            :headers {
                                      "Host" "www.courtauction.go.kr"
                                      "User-Agent" "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.8; ko; rv:1.9.0.14) Gecko/2009082706 Firefox/3.0.14"
                                      "Accept" "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
                                      "Accept-Language" "ko-KR,ko;q=0.8,en-US;q=0.5,en;q=0.3"
                                      "Accept-Encoding" "gzip,deflate"
                                      "Accept-Charset" "windows-949,utf-8;q=0.7,*;q=0.7"
                                      "Keep-Alive" "300"
                                      "Connection" "keep-alive"
                                      "Referer"	"https://www.courtauction.go.kr/RetrieveMainInfo.laf"
                                      }
                            :form-params {
                                          :_CUR_CMD	"RetrieveMainInfo.laf"
                                          :_CUR_SRNID	"PNO102000"
                                          :_FORM_YN	"Y"
                                          :_LOGOUT_CHK ""	
                                          :_NAVI_CMD ""
                                          :_NAVI_SRNID ""
                                          :_NEXT_CMD "RetrieveRealEstMulDetailList.laf"
                                          :_NEXT_SRNID "PNO102002"
                                          :_PRE_SRNID "PNO102001"
                                          :_SRCH_SRNID "PNO102000"
                                          :bubwLocGubun "2"
                                          :daepyoDongCd ""
                                          :daepyoSidoCd sido
                                          :daepyoSiguCd sigu
                                          :jibhgwanOffMgakPlcGubun ""
                                          :jiwonNm ""
                                          :mDaepyoDongCd ""	
                                          :mDaepyoSidoCd sido
                                          :mDaepyoSiguCd sigu
                                          :mvDaepyoSidoCd ""
                                          :mvDaepyoSiguCd ""
                                          :mvRealGbncd "1"
                                          :mvmPlaceDongCd ""
                                          :mvmPlaceSidoCd ""	
                                          :mvmPlaceSiguCd ""
                                          :notifyLoc "1"
                                          :notifyNewLoc "1"
                                          :notifyRealRoad "1"
                                          :rd1Cd ""
                                          :rd2Cd ""
                                          :rd3Rd4Cd ""
                                          :realVowel "00000_55203"
                                          :roadCode ""
                                          :roadPlaceSidoCd ""
                                          :roadPlaceSiguCd ""
                                          :srnID "PNO102000"
                                          :vowelSel "00000_55203" 
                                          }
                            }
                           )
      )
    (println "XXXXX" (:content (map (get [(getbyitemprop (:body resp) :class "Ltbl_list")] 0))))
    (doseq [node (:content (getbyitemprop (:body resp) :class "Ltbl_list"))]
      (println node)
      (if (= (:tag node) :tbody)
        (println "XXXXX, " node))
      )
;    (println (:body resp))
)
  )

(defn -main
  ([]
  (log/configure-logback "/courtauction-logback.xml")
    (config/config-yaml "/application-context.yaml") 
    (def sido (get (. (config/get-value :location.SidoCd) split ",") 0))
  (doseq [tSigu (config/get-value :location.SiguCd)]
      (def sigu (get (. tSigu split ",") 0))
    (get-list! sido sigu)
      )
    )
  )