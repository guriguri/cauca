(ns cauca.component.rest
 (:use compojure.core)
 (:use ring.middleware.reload)
 (:use [hiccup core page-helpers]) 
 (:use [ring.adapter.jetty :only [run-jetty]])
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

(defn check-validation [value min max regex isMatch error-msg]
  (if-not (nil? value)
    (if (false? (util/validation value min max regex isMatch))
      (throw (Exception. error-msg))
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
   
(defroutes main-routes
  (GET "/" [] (main-page))
  (GET ["/api/courtauction/:id", :id #"[0-9]+"] [id]
       (let [dao-impl# (f/get-obj :courtauction-dao)
             ret-courtauction (first (.get-courtauction dao-impl# id))
             json (json/write-str {:msg "ok" :result ret-courtauction}
                                  :value-fn cauca-writer :escape-unicode false)]
         (response json 200)))
  (GET "/api/courtauction" {params :query-params}
       (check-params params)
       (let [dao-impl# (f/get-obj :courtauction-dao)
             ret-courtauction-list (.get-courtauction-list dao-impl# params)
             json (json/write-str {:msg "ok" :result ret-courtauction-list}
                                  :value-fn cauca-writer :escape-unicode false)]
         (response json 200)))
  (route/not-found
    (let [json (json/write-str {:msg "page.not.found"})]
      (response json 404)))
  )
 
(def main-handler
  (-> main-routes handler/api))
 
(defn catch-errors [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        (let [json (json/write-str {:msg (.getMessage e)})]
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