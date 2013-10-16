(ns cauca.component.rest
 (:use compojure.core)
 (:use ring.middleware.reload)
 (:use [hiccup core page-helpers]) 
 (:use [ring.adapter.jetty :only [run-jetty]])
 (:require [compojure.route :as route]
           [compojure.handler :as handler]
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

 (defroutes main-routes
   (GET "/" [:as {cookies :cookies}]
        (-> (main-page)
            ui-template))
;   (GET "/topology/:id/component/:component" [:as {cookies :cookies} id component & m]
;        (let [include-sys? (get-include-sys? cookies)]
;          (-> (component-page id component (:window m) include-sys?)
;              (concat [(mk-system-toggle-button include-sys?)])
;              ui-template)))
;   (POST "/topology/:id/rebalance/:wait-time" [id wait-time]
;     (with-nimbus nimbus
;       (let [tplg (.getTopologyInfo ^Nimbus$Client nimbus id)
;             name (.get_name tplg)
;             options (RebalanceOptions.)]
;         (.set_wait_secs options (Integer/parseInt wait-time))
;         (.rebalance nimbus name options)
;         (log-message "Rebalancing topology '" name "' with wait time: " wait-time " secs")))
;     (resp/redirect (str "/topology/" id)))
   (route/resources "/")
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
