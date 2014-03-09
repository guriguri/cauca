(ns cauca.component.rest
 (:use [compojure.core]
       [ring.middleware.reload]
       [hiccup core page-helpers]
       [ring.adapter.jetty :only [run-jetty]]
       )
  (:require [cauca.factory :as f]
            [cauca.log :as log]
            [cauca.util :as util]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [clojure.data.json :as json]
            [ring.util.response :as resp]
            )
  (:gen-class)
  )

(defn main-page []
  (html5
    [:head
     [:title "CAUCA"]
     (include-css "/css/common.css")
     (include-js "/js/common.js")
     ]
    [:body
     [:h1 "CAUCA API"]
     [:ul 
      [:li "/api/courtauction"
       [:ul
        [:li "법원 경매 정보 리스트 조회"]
        [:li "method:get"]
        [:li "parameter"
         [:ul
          [:li "itemType: 경매 물건의 유형(근린시설, 기타, 다가구주택, 다세대, 단독주택, 대지, 빌라, 상가, 아파트, 연립주택, 오피스텔, 임야, 자동차, 전답, 중기 중 1개 선택)"]
          [:li "addr0: 시도 (서울특별시, 부산광역시, 대구광역시, 인천광역시, 광주광역시, 대전광역시, 울산광역시, 세종특별자치시, 경기도, 강원도, 충청북도, 충청남도, 전라북도, 전라남도, 경상북도, 경상남도, 제주특별자치도 중 1개 선택)"]
          [:li "addr1: 구군 (강남구, 은평구, 과천시, 고양시 등)"]
          [:li "minValue: 최소 경매가 (단위: 원)"]
          [:li "maxValue: 최대 경매가 (단위: 원)"]
          [:li "auctionStartDate: 조회하고 싶은 경매일 구간의 시작 (포멧: yyyy-mm-dd)"]
          [:li "auctionEndDate: 조회하고 싶은 경매일 구간의 종료 (포멧: yyyy-mm-dd)"]
          [:li "page: 조회하고 싶은 페이지 구간 (1-100, default: 1)"]
          [:li "pageSize: 한 페이지에 표시하고 싶은 경매정보 수 (1-100, default: 10)"]
          ]
         ]
        ]
       ]
      [:li "/api/courtauction/:id"
       [:ul
        [:li "법원 경매 정보 조회"]
        [:li "method:get"]
        [:li "parameter"
         [:ul
          [:li "id: 경매 물건 ID"]
          ]
         ]
        ]
       ]
      ]
     ]
    )
  )

(defn get-msg [args & request]
  (let [msg-source (f/get-obj :message-source)
        locale (util/get-locale request)]
    (try
      (.getMessage msg-source (last args), (to-array args), locale)
      (catch Exception e
        (do 
          (log/log-error e)
          (.getMessage msg-source "unknown.error", (to-array args), locale)
          )
        )
      )
    )
  )

(defn cauca-writer [key value]
  (if (or (= key :auctionDate) (= key :regDate) (= key :updDate))
    (str (java.sql.Date. (.getTime value)))
    value
    )
  )

(defn response [json status]
  (log/log-message json)
  (-> (resp/response json)
    (resp/status status)
    (resp/content-type "application/json; charset=utf-8")
    )
  )

(defn check-validation [value min max regex match? msg-id]
  (if-not (nil? value)
    (if (false? (util/validation value min max regex match?))
      (throw (Exception. msg-id))
      )
    )
  )
 
(defn check-params [params]
  (check-validation (params "page") 1 3 #"[0-9]+" true "invalid.param.page")
  (check-validation (params "pageSize") 1 3 #"[0-9]+" true "invalid.param.pageSize")
  (check-validation (params "itemType") nil nil #".*[\s~!@#$%^&*()_+`\\=\-{}|\\[\\]:\\\\\";'<>?,./].*" false "invalid.param.itemType")
  (check-validation (params "addr0") nil nil #".*[\s~!@#$%^&*()_+`\\=\-{}|\\[\\]:\\\\\";'<>?,./].*" false "invalid.param.addr0")
  (check-validation (params "addr1") nil nil #".*[\s~!@#$%^&*()_+`\\=\-{}|\\[\\]:\\\\\";'<>?,./].*" false "invalid.param.addr1")
  (check-validation (params "auctionStartDate") 9 11 #"20[0-9][0-9]-[0-1][0-9]-[0-3][0-9]" true "invalid.param.auctionStartDate")
  (check-validation (params "auctionEndDate") 9 11 #"20[0-9][0-9]-[0-1][0-9]-[0-3][0-9]" true "invalid.param.auctionEndDate")
  )

(defn get-json [map-obj]
  (json/write-str map-obj :value-fn cauca-writer :escape-unicode false)
  )
   
(defroutes main-routes
  (GET "/" [] (main-page))
  (GET ["/api/courtauction/:id", :id #"[0-9]+"] [id]
       (let [dao-impl# (f/get-obj :courtauction-dao)
             ret-courtauction (first (.get-courtauction dao-impl# id))
             json (get-json {:msg "ok" :result ret-courtauction})]
         (response json 200)))
;  (GET "/api/courtauction" {params :query-params}
  (GET "/api/courtauction" request
;       (log/log-message request)
       (check-params (request :query-params))
       (let [params (request :query-params)
             dao-impl# (f/get-obj :courtauction-dao)
             ret-courtauction-list (.get-courtauction-list dao-impl# params)
             json (get-json {:msg "ok" :result ret-courtauction-list})]
         (response json 200)))
  (route/resources "/")
  (route/not-found 
    (let [json (get-json {:msg (get-msg ["page.not.found"])})]
      (response json 404)))
  )
 
(def main-handler
  (-> main-routes handler/api))
 
(defn catch-errors [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        (let [json (get-json {:msg (get-msg [(.getMessage e)] request)})]
          (response json 400))
        )
      )
    )
  )

(def app
  (-> #'main-handler
    (wrap-reload '[cauca.component.rest])
    catch-errors))

(defn start-server! []
  (run-jetty app {:port 8080 :join? false})
  )

(defn -main []
  (start-server!)
  )