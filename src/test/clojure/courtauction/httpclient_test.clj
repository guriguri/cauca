(ns courtauction.httpclient-test
  (:use [clojure test]      
        ) 
  (:require [courtauction.component.application-context :as application-context]
            [courtauction.log :as log]
            [courtauction.config :as config]
            [clj-http.client :as client])
  )

(deftest get-list
  (config/config-yaml "/application-context.yaml")
  (doseq [tmp (config/get-value :location.codes)]
    (do
      (def info (. tmp split ","))
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
                              "Cookie" "WMONID=MwsvVLqo_MW; daepyoSidoCd=41; daepyoSiguCd=410; mvmPlaceSidoCd=; mvmPlaceSiguCd=; rd1Cd=; rd2Cd=; realVowel=35207_45207; roadPlaceSidoCd=; roadPlaceSiguCd=; vowelSel=35207_45207; page=default20"
                              }
                    :form-params {:_CUR_CMD	"RetrieveMainInfo.laf"
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
                                  :daepyoSidoCd "41"
                                  :daepyoSiguCd "410"
                                  :jibhgwanOffMgakPlcGubun ""
                                  :jiwonNm ""
                                  :mDaepyoDongCd ""	
                                  :mDaepyoSidoCd "41"
                                  :mDaepyoSiguCd "410"
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
                    }}
                   ))
      (println (:body resp))
;      (println (get info 0) (get info 1))
      )
    )
  )