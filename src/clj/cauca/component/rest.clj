(ns cauca.component.rest
 (:use compojure.core)
 (:use ring.middleware.reload)
 (:use [hiccup core page-helpers]) 
 (:use [ring.adapter.jetty :only [run-jetty]])
 (:require [cauca.factory :as f]
           [cauca.log :as log]
           [compojure.route :as route]
           [compojure.handler :as handler]
           [clojure.data.json :as json]
           [ring.util.response :as resp]
           )
 (:gen-class))

 (defn main-page []
   (html5
    [:head
     [:title "CAUCA"]
     (include-css "/css/bootstrap-1.1.0.css")
     (include-css "/css/style.css")
     (include-js "/js/jquery-1.6.2.min.js")
     (include-js "/js/jquery.tablesorter.min.js")
     (include-js "/js/jquery.cookies.2.2.0.min.js")
     (include-js "/js/script.js")
     ]
    [:body
     [:h1 "CAUCA API"]
     [:ul 
      [:li "/courtauction"
       [:ul
        [:li "법원 경매 정보 리스트 조회"]
        [:li "method:get"]
       ]
       ]
      [:li "/courtauction/:id"
       [:ul
        [:li "법원 경매 정보 조회"]
        [:li "method:get"]
       ]
       ]
      ]
     ]
    ))

 (defn cauca-writer [key value]
  (if (or (= key :auctionDate) (= key :regDate) (= key :updDate))
    (str (java.sql.Date. (.getTime value)))
    value))

 (defroutes main-routes
   (GET "/" [] (main-page))
   (GET "/api/courtauction/:id" [id]
        (let [dao-impl# (f/get-obj :courtauction-dao)
              ret-courtauction (first (.get-courtauction dao-impl# id))
              json (json/write-str ret-courtauction :value-fn cauca-writer :escape-unicode false)]
          (log/log-message json)
          (-> (resp/response json)
            (resp/content-type "application/json; charset=utf-8"))))
   (GET "/api/courtauction" {params :query-params}
        (let [dao-impl# (f/get-obj :courtauction-dao)
              ret-courtauction-list (.get-courtauction-list dao-impl# params)
              json (json/write-str ret-courtauction-list :value-fn cauca-writer :escape-unicode false)]
          (log/log-message json)
          (-> (resp/response json)
            (resp/content-type "application/json; charset=utf-8"))))
;        (str "itemType:" (params "itemType") "<br/>"
;             "addr0:" (params "addr0") "<br/>"
;             "addr1:" (params "addr1") "<br/>"
;             "value:" (params "value") "<br/>"
;             "valueMin:" (params "valueMin") "<br/>"
;             "auctionDate:" (params "auctionDate")))
   (route/not-found "Page not found"))
 
 (def main-handler (-> main-routes handler/api))

 (defn exception->html [e]
   (concat
     [[:h2 "Internal Server Error"]]
     [[:pre (let [sw (java.io.StringWriter.)]
       (.printStackTrace e (java.io.PrintWriter. sw))
       (.toString sw))]]))

 (defn catch-errors [handler]
   (fn [request]
     (try
       (handler request)
       (catch Exception e
         (-> (resp/response (exception->html e))
           (resp/status 500)
           (resp/content-type "text/html"))
         ))))

 (def app
   (-> #'main-handler
       (wrap-reload '[cauca.component.rest])
       catch-errors))

 (defn start-server! [] (run-jetty app {:port 8080
                                        :join? false}))
 (defn -main [] (start-server!))
