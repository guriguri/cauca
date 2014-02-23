(ns cauca.component.rest-test
  (:use [clojure test]      
        ) 
  (:require [cauca.factory :as f]
            [cauca.component.rest :as rest]
            [cauca.log :as log]
            [cauca.config :as config]
            )
  )

(defn request [method resource web-app & params]
  (web-app {:request-method method :uri resource :query-params (first params)}))

(deftest test-routes
  (log/configure-logback "/cauca-logback.xml")
  (config/config-yaml "/cauca-context.yaml") 
  (is (= 200 (:status (request :get "/" rest/main-routes))))
  (is (= 200 (:status (request :get "/api/courtauction/691438" rest/main-routes))))
  (is (= 200 (:status (request :get "/api/courtauction" rest/main-routes {:page 1 :pageSize 10})))))