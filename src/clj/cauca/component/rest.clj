(ns cauca.component.rest
 (:use compojure.core)
 (:use ring.middleware.reload)
 (:use [hiccup core page-helpers]) 
 (:use [ring.adapter.jetty :only [run-jetty]])
 (:require [cauca.factory :as f]
           [compojure.route :as route]
           [compojure.handler :as handler]
           [clojure.data.json :as json]
           [ring.util.response :as resp]
           )
 (:gen-class))

 (defn ui-template [body]
   (html4
    [:head
     [:title "Cauca UI"]
     (include-css "/css/bootstrap-1.1.0.css")
     (include-css "/css/style.css")
     (include-js "/js/jquery-1.6.2.min.js")
     (include-js "/js/jquery.tablesorter.min.js")
     (include-js "/js/jquery.cookies.2.2.0.min.js")
     (include-js "/js/script.js")
     ]
    [:body
     [:h1 (link-to "/" "Cauca UI")]
     (seq body)
     ]))

 (defn main-page []
   (concat [[:h1 "Cauca Main"]])
   )
 
 (defn my-value-writer [key value]
  (if (or (= key :auctionDate) (= key :regDate) (= key :updDate))
    (str (java.sql.Date. (.getTime value)))
    value))

 (defroutes main-routes
   (GET "/" [:as {cookies :cookies}]
        (-> (main-page)
            ui-template))
   (GET "/courtauction/:id" [id]
        (let [dao-impl# (f/get-obj :courtauction-dao)
              ret-courtauction (first (.get-courtauction dao-impl# id))]
          (json/write-str ret-courtauction
                          :value-fn my-value-writer
                          )))
   (route/not-found "Page not found"))

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
         (-> (resp/response (ui-template (exception->html e)))
           (resp/status 500)
           (resp/content-type "text/html"))
         ))))

 (def app
   (-> #'main-routes
       (wrap-reload '[cauca.component.rest])
       catch-errors))

 (defn start-server! [] (run-jetty app {:port 8080
                                        :join? false}))

 (defn -main [] (start-server!))
