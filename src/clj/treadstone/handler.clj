(println "loading treadstone.handler")

(ns treadstone.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [treadstone.layout :refer [error-page]]
            [treadstone.routes.home :refer [home-routes]]
            [treadstone.routes.services :refer [service-routes]]
            [compojure.route :as route]
            [treadstone.middleware :as middleware]

            ;; [treadstone.routes.websockets :refer [websocket-routes]]
            [murphydye.websockets.core :refer [websocket-routes]]
            [murphydye.components]
            ))

(def app-routes
  (routes
    #'websocket-routes
    #'service-routes
    (wrap-routes #'home-routes middleware/wrap-csrf)
    (route/not-found
      (:body
        (error-page {:status 404
                     :title "page not found"})))))

(println "almost done loading treadstone.handler")

(def app (middleware/wrap-base #'app-routes))
;; (def app #'app-routes)
;; (println middleware/wrap-base)

(println "done loading treadstone.handler")
